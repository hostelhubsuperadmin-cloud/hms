
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  RefreshCw,
  Receipt,
  CreditCard,
  Upload,
  ShieldCheck,
  Wallet,
  ArrowUpRight,
  CircleCheck,
  Clock3,
  AlertCircle,
} from "lucide-react";

import paymentService from "../../services/paymentService";
import { getApiError } from "../../api/axiosClient";
import "./HostlerPaymentsPage.css";

const RAZORPAY_SCRIPT =
  "https://checkout.razorpay.com/v1/checkout.js";

function money(value, currency = "INR") {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: currency || "INR",
    maximumFractionDigits: 2,
  }).format(Number(value || 0));
}

function dateLabel(value) {
  if (!value) return "—";

  return new Date(
    value.length === 10 ? `${value}T00:00:00` : value
  ).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function errorMessage(error) {
  return (
    getApiError(error) ||
    error?.response?.data?.message ||
    error?.message ||
    "Something went wrong. Please try again."
  );
}

function StatusBadge({ status }) {
  const value = String(status || "UNKNOWN").toUpperCase();

  let tone = "neutral";

  if (["PAID", "SUCCESS"].includes(value)) tone = "success";
  else if (
    ["PENDING", "SUBMITTED", "PROCESSING", "PARTIALLY_PAID"].includes(value)
  ) tone = "warning";
  else if (
    ["OVERDUE", "FAILED", "REJECTED", "RECONCILIATION_REQUIRED"].includes(value)
  ) tone = "danger";

  return (
    <span className={`hp-status hp-status-${tone}`}>
      {value.replaceAll("_", " ")}
    </span>
  );
}

function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const existing = document.querySelector(
      `script[src="${RAZORPAY_SCRIPT}"]`
    );

    if (existing) {
      existing.addEventListener("load", () => resolve(true), { once: true });
      existing.addEventListener("error", () => resolve(false), { once: true });
      return;
    }

    const script = document.createElement("script");
    script.src = RAZORPAY_SCRIPT;
    script.async = true;
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

export default function HostlerPaymentsPage() {
  const [invoices, setInvoices] = useState([]);
  const [payments, setPayments] = useState([]);

  const [invoiceId, setInvoiceId] = useState("");
  const [amount, setAmount] = useState("");
  const [manualReference, setManualReference] = useState("");
  const [proofFile, setProofFile] = useState(null);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const loadData = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const [invoiceData, paymentData] = await Promise.all([
        paymentService.getMyInvoices(),
        paymentService.getMyPayments(),
      ]);

      setInvoices(Array.isArray(invoiceData) ? invoiceData : []);
      setPayments(Array.isArray(paymentData) ? paymentData : []);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const payableInvoices = useMemo(
    () =>
      invoices.filter(
        (invoice) =>
          Number(invoice.balance) > 0 &&
          ["ISSUED", "PARTIALLY_PAID", "OVERDUE"].includes(
            String(invoice.status).toUpperCase()
          )
      ),
    [invoices]
  );

  const selectedInvoice = payableInvoices.find(
    (invoice) => String(invoice.id) === String(invoiceId)
  );

  const outstandingTotal = invoices.reduce(
    (sum, invoice) => sum + Math.max(0, Number(invoice.balance || 0)),
    0
  );

  const paidTotal = invoices.reduce(
    (sum, invoice) => sum + Number(invoice.amountPaid || 0),
    0
  );

  function selectInvoice(value) {
    setInvoiceId(value);

    const invoice = payableInvoices.find(
      (item) => String(item.id) === String(value)
    );

    setAmount(invoice ? String(invoice.balance) : "");
  }

  function validateAmount() {
    const numericAmount = Number(amount);

    if (!selectedInvoice) {
      setError("Choose an outstanding invoice first.");
      return false;
    }

    if (!Number.isFinite(numericAmount) || numericAmount <= 0) {
      setError("Enter a valid payment amount.");
      return false;
    }

    if (numericAmount > Number(selectedInvoice.balance)) {
      setError("Payment cannot exceed the outstanding invoice balance.");
      return false;
    }

    if (selectedInvoice.currency && selectedInvoice.currency !== "INR") {
      setError("Razorpay checkout currently expects an INR invoice.");
      return false;
    }

    return true;
  }

  async function submitManualPayment(event) {
    event.preventDefault();
    setError("");
    setNotice("");

    if (!validateAmount()) return;

    setSaving(true);

    try {
      const payment = await paymentService.submitManualPayment({
        invoiceId,
        amount,
        manualReference,
      });

      let proofMessage = "";

      if (proofFile) {
        try {
          await paymentService.uploadProof(payment.id, proofFile);
        } catch (uploadError) {
          proofMessage =
            ` Payment #${payment.id} was submitted, but proof upload failed: ` +
            errorMessage(uploadError);
        }
      }

      setNotice(
        `Manual payment #${payment.id} submitted for review. ` +
          "The invoice balance changes only after approval." +
          proofMessage
      );

      setAmount("");
      setManualReference("");
      setProofFile(null);

      const fileInput = document.getElementById("hp-proof-file");
      if (fileInput) fileInput.value = "";

      await loadData();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function payWithRazorpay() {
    setError("");
    setNotice("");

    if (!validateAmount()) return;

    setSaving(true);

    try {
      const loaded = await loadRazorpayScript();

      if (!loaded) {
        throw new Error(
          "Razorpay Checkout could not load. Check your internet connection."
        );
      }

      const order = await paymentService.createRazorpayOrder({
        invoiceId,
        amount,
      });

      if (!order.razorpayKeyId || !order.razorpayOrderId) {
        throw new Error(
          "The backend did not return the Razorpay checkout configuration."
        );
      }

      const checkout = new window.Razorpay({
        key: order.razorpayKeyId,
        amount: order.amountPaise,
        currency: order.currency || "INR",
        name: order.name || "HostelHub",
        description:
          order.description || `Payment for invoice #${order.invoiceId}`,
        order_id: order.razorpayOrderId,

        handler: async (response) => {
          setSaving(true);
          setError("");

          try {
            const result = await paymentService.verifyRazorpayPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });

            setNotice(
              result.message ||
                "Payment verification was submitted successfully."
            );

            await loadData();
          } catch (err) {
            setError(
              "Checkout returned, but backend verification did not complete. " +
                "Refresh payment history before trying again. " +
                errorMessage(err)
            );
          } finally {
            setSaving(false);
          }
        },

        modal: {
          ondismiss: () => {
            setNotice(
              "Checkout was closed. Check payment history before retrying."
            );
            setSaving(false);
          },
        },

        theme: { color: "#2563eb" },
      });

      checkout.on("payment.failed", (response) => {
        setError(
          response?.error?.description ||
            "Razorpay payment failed. You can try again."
        );
        setSaving(false);
      });

      checkout.open();
    } catch (err) {
      setError(errorMessage(err));
      setSaving(false);
    }
  }

  return (
    <main className="hp-page">
      <section className="hp-hero">
        <div>
          <span className="hp-eyebrow">RESIDENT BILLING</span>
          <h1>Invoices & payments</h1>
          <p>
            Review your hostel charges, pay securely, and track every
            transaction in one place.
          </p>
        </div>

        <button
          className="hp-button hp-button-light"
          type="button"
          onClick={loadData}
          disabled={loading || saving}
        >
          <RefreshCw size={16} />
          Refresh
        </button>
      </section>

      {error && (
        <div className="hp-alert hp-alert-error" role="alert">
          <AlertCircle size={19} />
          <span>{error}</span>
        </div>
      )}

      {notice && (
        <div className="hp-alert hp-alert-success" role="status">
          <CircleCheck size={19} />
          <span>{notice}</span>
        </div>
      )}

      <section className="hp-summary-grid">
        <article className="hp-summary-card">
          <span className="hp-summary-icon hp-icon-blue">
            <Receipt size={21} />
          </span>
          <div>
            <p>Total invoices</p>
            <strong>{invoices.length}</strong>
            <small>Invoices issued to you</small>
          </div>
        </article>

        <article className="hp-summary-card">
          <span className="hp-summary-icon hp-icon-orange">
            <Wallet size={21} />
          </span>
          <div>
            <p>Outstanding balance</p>
            <strong>{money(outstandingTotal)}</strong>
            <small>Amount remaining to pay</small>
          </div>
        </article>

        <article className="hp-summary-card">
          <span className="hp-summary-icon hp-icon-green">
            <ShieldCheck size={21} />
          </span>
          <div>
            <p>Amount paid</p>
            <strong>{money(paidTotal)}</strong>
            <small>Recorded against your invoices</small>
          </div>
        </article>
      </section>

      <section className="hp-panel">
        <div className="hp-panel-heading">
          <div>
            <span className="hp-eyebrow">MAKE A PAYMENT</span>
            <h2>Pay an outstanding invoice</h2>
            <p>Choose the invoice and amount, then select your payment method.</p>
          </div>
          <CreditCard size={25} />
        </div>

        {loading ? (
          <div className="hp-loading">Loading invoices and payments…</div>
        ) : (
          <form onSubmit={submitManualPayment}>
            <div className="hp-form-grid">
              <label className="hp-field">
                <span>Invoice</span>
                <select
                  value={invoiceId}
                  onChange={(event) => selectInvoice(event.target.value)}
                  required
                >
                  <option value="">Select an invoice</option>
                  {payableInvoices.map((invoice) => (
                    <option key={invoice.id} value={invoice.id}>
                      Invoice #{invoice.id} · Due {dateLabel(invoice.dueDate)} ·
                      Balance {money(invoice.balance, invoice.currency)}
                    </option>
                  ))}
                </select>
              </label>

              <label className="hp-field">
                <span>Amount (INR)</span>
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  max={selectedInvoice?.balance ?? undefined}
                  value={amount}
                  onChange={(event) => setAmount(event.target.value)}
                  placeholder="Enter amount"
                  required
                />
              </label>

              <label className="hp-field hp-field-wide">
                <span>UPI / bank reference (manual payment only)</span>
                <input
                  value={manualReference}
                  onChange={(event) => setManualReference(event.target.value)}
                  maxLength={100}
                  placeholder="Enter transaction reference, if available"
                />
              </label>

              <label className="hp-field hp-field-wide">
                <span>Payment proof (optional, manual payment only)</span>
                <div className="hp-file-picker">
                  <Upload size={18} />
                  <input
                    id="hp-proof-file"
                    type="file"
                    accept=".jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf"
                    onChange={(event) =>
                      setProofFile(event.target.files?.[0] || null)
                    }
                  />
                </div>
                <small>JPG, PNG or PDF. Maximum file size: 5 MB.</small>
              </label>
            </div>

            {selectedInvoice && (
              <div className="hp-selected-invoice">
                <div>
                  <span>Invoice total</span>
                  <strong>
                    {money(selectedInvoice.subtotal, selectedInvoice.currency)}
                  </strong>
                </div>
                <div>
                  <span>Already paid</span>
                  <strong>
                    {money(selectedInvoice.amountPaid, selectedInvoice.currency)}
                  </strong>
                </div>
                <div>
                  <span>Remaining</span>
                  <strong className="hp-balance">
                    {money(selectedInvoice.balance, selectedInvoice.currency)}
                  </strong>
                </div>
              </div>
            )}

            <div className="hp-payment-methods">
              <button
                className="hp-button hp-button-primary"
                type="button"
                onClick={payWithRazorpay}
                disabled={saving || loading || !selectedInvoice}
              >
                <CreditCard size={17} />
                {saving ? "Processing…" : "Pay with Razorpay"}
                <ArrowUpRight size={16} />
              </button>

              <button
                className="hp-button hp-button-outline"
                type="submit"
                disabled={saving || loading || !selectedInvoice}
              >
                <Upload size={17} />
                {saving ? "Submitting…" : "Submit manual payment"}
              </button>
            </div>

            <p className="hp-security-note">
              <ShieldCheck size={16} />
              Razorpay handles online checkout. HostelHub verifies payments
              with the backend before updating your invoice balance.
            </p>

            {payableInvoices.length === 0 && (
              <p className="hp-empty-inline">
                There are no outstanding invoices available for payment.
              </p>
            )}
          </form>
        )}
      </section>

      <section className="hp-panel">
        <div className="hp-panel-heading">
          <div>
            <span className="hp-eyebrow">BILLING OVERVIEW</span>
            <h2>Your invoices</h2>
            <p>Invoice totals and balances are provided by the backend.</p>
          </div>
          <Receipt size={25} />
        </div>

        {invoices.length === 0 && !loading ? (
          <div className="hp-empty-state">
            <Receipt size={32} />
            <strong>No invoices yet</strong>
            <p>Your hostel will display an invoice here when one is issued.</p>
          </div>
        ) : (
          <div className="hp-table-wrap">
            <table className="hp-table">
              <thead>
                <tr>
                  <th>Invoice</th>
                  <th>Billing period</th>
                  <th>Due date</th>
                  <th>Total</th>
                  <th>Paid</th>
                  <th>Balance</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {invoices.map((invoice) => (
                  <tr key={invoice.id}>
                    <td>
                      <strong>#{invoice.id}</strong>
                    </td>
                    <td>
                      {dateLabel(invoice.periodStart)} –{" "}
                      {dateLabel(invoice.periodEnd)}
                    </td>
                    <td>{dateLabel(invoice.dueDate)}</td>
                    <td>{money(invoice.subtotal, invoice.currency)}</td>
                    <td>{money(invoice.amountPaid, invoice.currency)}</td>
                    <td>
                      <strong>{money(invoice.balance, invoice.currency)}</strong>
                    </td>
                    <td><StatusBadge status={invoice.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="hp-panel">
        <div className="hp-panel-heading">
          <div>
            <span className="hp-eyebrow">TRANSACTION LOG</span>
            <h2>Payment history</h2>
            <p>Track online payments and manual payments under review.</p>
          </div>
          <Clock3 size={25} />
        </div>

        {payments.length === 0 && !loading ? (
          <div className="hp-empty-state">
            <Wallet size={32} />
            <strong>No payment transactions</strong>
            <p>Your submitted payments will appear here.</p>
          </div>
        ) : (
          <div className="hp-table-wrap">
            <table className="hp-table">
              <thead>
                <tr>
                  <th>Payment</th>
                  <th>Invoice</th>
                  <th>Method</th>
                  <th>Reference</th>
                  <th>Amount</th>
                  <th>Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {payments.map((payment) => (
                  <tr key={payment.id}>
                    <td><strong>#{payment.id}</strong></td>
                    <td>#{payment.invoiceId}</td>
                    <td>{payment.method}</td>
                    <td>
                      {payment.manualReference ||
                        payment.gatewayPaymentId ||
                        payment.providerReference ||
                        "—"}
                      {payment.proofOriginalFilename && (
                        <small className="hp-proof-name">
                          Proof: {payment.proofOriginalFilename}
                        </small>
                      )}
                    </td>
                    <td>{money(payment.amount, payment.currency)}</td>
                    <td>{dateLabel(payment.createdAt)}</td>
                    <td><StatusBadge status={payment.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}
