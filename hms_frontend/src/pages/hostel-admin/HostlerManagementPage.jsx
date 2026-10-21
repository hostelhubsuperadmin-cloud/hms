import { useCallback, useEffect, useMemo, useState } from "react";
import {
  BedDouble,
  Building2,
  CalendarCheck,
  CheckCircle2,
  ClipboardList,
  CreditCard,
  FileText,
  Plus,
  RefreshCw,
  Search,
  Users,
  X,
} from "lucide-react";

import hostlerManagementService from "../../services/hostlerManagementService";
import { getApiError } from "../../api/axiosClient";
import "./HostlerManagementPage.css";

const EMPTY_HOSTLER = {
  fullName: "",
  email: "",
  phone: "",
  dateOfBirth: "",
  gender: "",
  guardianName: "",
  guardianPhone: "",
  emergencyContact: "",
  addressLine: "",
  city: "",
  state: "",
  postalCode: "",
  notes: "",
};

const EMPTY_ALLOCATION = {
  hostlerId: "",
  bedId: "",
  startDate: "",
  plannedCheckoutDate: "",
};

const EMPTY_INVOICE = {
  allocationId: "",
  periodStart: "",
  periodEnd: "",
  dueDate: "",
};

const PAYMENT_FILTERS = [
  { value: "SUBMITTED", label: "Pending review" },
  { value: "PENDING", label: "Pending" },
  { value: "PROCESSING", label: "Processing" },
  { value: "SUCCESS", label: "Successful" },
  { value: "PARTIALLY_PAID", label: "Partially paid" },
  { value: "REJECTED", label: "Rejected" },
  { value: "FAILED", label: "Failed" },
  { value: "REFUNDED", label: "Refunded" },
  { value: "ALL", label: "All payments" },
];
// Payment proof preview

function getMessage(error) {
  try {
    const message = getApiError?.(error);

    if (message) {
      return typeof message === "string"
        ? message
        : "The request failed. Please try again.";
    }
  } catch {
    // Fall back to the normal Axios error fields below.
  }

  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    "Something went wrong. Please try again."
  );
}

function today() {
  const now = new Date();
  const localDate = new Date(
    now.getTime() - now.getTimezoneOffset() * 60000
  );

  return localDate.toISOString().slice(0, 10);
}

function money(value, currency = "INR") {
  const amount = Number(value ?? 0);

  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: currency || "INR",
    maximumFractionDigits: 2,
  }).format(Number.isFinite(amount) ? amount : 0);
}

function dateLabel(value) {
  if (!value) return "—";

  const date = /^\d{4}-\d{2}-\d{2}$/.test(String(value))
    ? new Date(`${value}T00:00:00`)
    : new Date(value);

  if (Number.isNaN(date.getTime())) return "—";

  return date.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function dateTimeLabel(value) {
  if (!value) return "—";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return "—";

  return date.toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function statusValue(status) {
  return String(status || "UNKNOWN").toUpperCase();
}

function StatusPill({ status }) {
  const value = statusValue(status);

  let tone = "neutral";

  if (
    [
      "ACTIVE",
      "AVAILABLE",
      "PAID",
      "SUCCESS",
      "COMPLETED",
      "APPROVED",
    ].includes(value)
  ) {
    tone = "success";
  } else if (
    [
      "RESERVED",
      "SUBMITTED",
      "PENDING",
      "PARTIALLY_PAID",
      "PROCESSING",
      "ISSUED",
    ].includes(value)
  ) {
    tone = "warning";
  } else if (
    [
      "OVERDUE",
      "REJECTED",
      "CANCELLED",
      "INACTIVE",
      "FAILED",
      "VOID",
      "REFUNDED",
    ].includes(value)
  ) {
    tone = "danger";
  }

  return (
    <span className={`hm-status ${tone}`}>
      {value.replaceAll("_", " ")}
    </span>
  );
}

function EmptyState({ title, message }) {
  return (
    <div className="hm-empty">
      <ClipboardList size={28} />
      <strong>{title}</strong>
      <p>{message}</p>
    </div>
  );
}

function getInvoiceBalance(invoice) {
  if (invoice.balance != null) {
    return Math.max(0, Number(invoice.balance) || 0);
  }

  const total = Number(invoice.subtotal ?? 0);
  const paid = Number(invoice.amountPaid ?? 0);

  return Math.max(0, total - paid);
}

function getInvoiceTotal(invoice) {
  return Number(invoice.subtotal ?? invoice.totalAmount ?? 0);
}

function getInvoiceHostlerId(invoice) {
  return invoice.hostlerId ?? invoice.hostler?.hostlerId ?? "—";
}

function getPaymentReference(payment) {
  return (
    payment.manualReference ||
    payment.providerReference ||
    payment.gatewayPaymentId ||
    "—"
  );
}

export default function HostlerManagementPage() {
  const [managedHostels, setManagedHostels] = useState([]);
  const [hostelId, setHostelId] = useState("");

  const [hostlers, setHostlers] = useState([]);
  const [availableBeds, setAvailableBeds] = useState([]);
  const [allocations, setAllocations] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [payments, setPayments] = useState([]);

  const [tab, setTab] = useState("hostlers");
  const [search, setSearch] = useState("");
  const [hostlerStatus, setHostlerStatus] = useState("ALL");
  const [paymentStatus, setPaymentStatus] = useState("SUBMITTED");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const [showHostlerForm, setShowHostlerForm] = useState(false);
  const [editingHostler, setEditingHostler] = useState(null);
  const [hostlerForm, setHostlerForm] = useState(EMPTY_HOSTLER);

  const [allocationForm, setAllocationForm] = useState(EMPTY_ALLOCATION);
  const [allocationHostlerSearch, setAllocationHostlerSearch] = useState("");
  const [allocationHostlerPickerOpen, setAllocationHostlerPickerOpen] = useState(false);
  const [invoiceForm, setInvoiceForm] = useState(EMPTY_INVOICE);
const [allocationSearch, setAllocationSearch] = useState("");
const [allocationPickerOpen, setAllocationPickerOpen] = useState(false);
const [eligibleAllocationIds, setEligibleAllocationIds] = useState(null);
const [eligibleAllocationsLoading, setEligibleAllocationsLoading] = useState(false);
const [eligibleAllocationsError, setEligibleAllocationsError] = useState("");
const [selectedPaymentProof, setSelectedPaymentProof] = useState(null);
const [proofPreviewUrl, setProofPreviewUrl] = useState("");
const [proofPreviewContentType, setProofPreviewContentType] = useState("");
const [proofPreviewLoading, setProofPreviewLoading] = useState(false);
const [proofPreviewError, setProofPreviewError] = useState("");
  const selectedHostel = useMemo(
    () =>
      managedHostels.find(
        (item) => String(item.hostelId) === String(hostelId)
      ),
    [managedHostels, hostelId]
  );

  const loadManagedHostels = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const result = await hostlerManagementService.getManagedHostels();
      const list = Array.isArray(result) ? result : [];

      setManagedHostels(list);

      setHostelId((current) => {
        const stillAssigned = list.some(
          (item) => String(item.hostelId) === String(current)
        );

        if (current && stillAssigned) {
          return String(current);
        }

        return list[0]?.hostelId != null
          ? String(list[0].hostelId)
          : "";
      });
    } catch (err) {
      setManagedHostels([]);
      setHostelId("");
      setError(getMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  const loadWorkspace = useCallback(async () => {
    if (!hostelId) {
      setHostlers([]);
      setAvailableBeds([]);
      setAllocations([]);
      setInvoices([]);
      setPayments([]);
      return;
    }

    setLoading(true);
    setError("");

    try {
      const results = await Promise.allSettled([
        hostlerManagementService.getHostlers(hostelId),
        hostlerManagementService.getAvailableBeds(hostelId),
        hostlerManagementService.getAllocations(hostelId),
        hostlerManagementService.getInvoices(hostelId),
        hostlerManagementService.getPayments(hostelId, paymentStatus),
      ]);

      const setters = [
        setHostlers,
        setAvailableBeds,
        setAllocations,
        setInvoices,
        setPayments,
      ];

      const labels = [
        "Hostlers",
        "Available beds",
        "Allocations",
        "Invoices",
        "Payments",
      ];

      const errors = [];

      results.forEach((result, index) => {
        if (result.status === "fulfilled") {
          setters[index](
            Array.isArray(result.value) ? result.value : []
          );
        } else {
          // Keep the page usable if one independent request fails.
          setters[index]([]);
          errors.push(
            `${labels[index]}: ${getMessage(result.reason)}`
          );
        }
      });

      if (errors.length > 0) {
        setError(errors.join(" "));
      }
    } finally {
      setLoading(false);
    }
  }, [hostelId, paymentStatus]);

  useEffect(() => {
    loadManagedHostels();
  }, [loadManagedHostels]);

  useEffect(() => {
    loadWorkspace();
  }, [loadWorkspace]);

  useEffect(() => {
    let cancelled = false;

    async function loadEligibleAllocationIds() {
      const { periodStart, periodEnd } = invoiceForm;

      setEligibleAllocationIds(null);
      setEligibleAllocationsError("");

      if (
        !hostelId ||
        !periodStart ||
        !periodEnd ||
        periodEnd < periodStart
      ) {
        setEligibleAllocationsLoading(false);
        return;
      }

      setEligibleAllocationsLoading(true);

      try {
        const ids = await hostlerManagementService.getEligibleInvoiceAllocationIds(
          hostelId,
          periodStart,
          periodEnd
        );

        if (!cancelled) {
          setEligibleAllocationIds(
            Array.isArray(ids) ? ids.map((id) => String(id)) : []
          );
        }
      } catch (requestError) {
        if (!cancelled) {
          // Keep the picker usable if this optional eligibility endpoint is
          // unavailable; the invoice-generation endpoint remains authoritative.
          setEligibleAllocationIds(null);
          setEligibleAllocationsError(getMessage(requestError));
        }
      } finally {
        if (!cancelled) {
          setEligibleAllocationsLoading(false);
        }
      }
    }

    loadEligibleAllocationIds();

    return () => {
      cancelled = true;
    };
  }, [hostelId, invoiceForm.periodStart, invoiceForm.periodEnd]);

  const eligibleAllocations = useMemo(() => {
    const query = allocationSearch.trim().toLowerCase();
    const periodStart = invoiceForm.periodStart;
    const periodEnd = invoiceForm.periodEnd;

    if (
      !periodStart ||
      !periodEnd ||
      periodEnd < periodStart
    ) {
      return [];
    }

    return allocations.filter((item) => {
      const allocationId = item.id ?? item.allocationId;
      if (allocationId == null) return false;

      const status = statusValue(item.status);
      const cancelledStatus = ["CANCELLED", "CANCELED", "VOID", "REJECTED"].includes(status);

      if (eligibleAllocationIds !== null) {
        if (!eligibleAllocationIds.includes(String(allocationId))) {
          return false;
        }
      } else if (status !== "ACTIVE" && status !== "COMPLETED") {
        // Fallback used only if the eligibility API cannot be reached.
        return false;
      }

      if (cancelledStatus) return false;

      const alreadyInvoiced = invoices.some((invoice) => {
        const invoiceAllocationId =
          invoice.allocationId ??
          invoice.allocation?.id ??
          invoice.allocation?.allocationId;

        return (
          String(invoiceAllocationId) === String(allocationId) &&
          String(invoice.periodStart ?? "").slice(0, 10) === periodStart &&
          String(invoice.periodEnd ?? "").slice(0, 10) === periodEnd
        );
      });

      if (alreadyInvoiced) {
        return false;
      }

      const searchableText = [
        allocationId,
        item.hostlerId,
        item.hostlerName,
        item.hostlerEmail,
        item.email,
        item.bedNumber,
        item.roomNumber,
        item.flatName,
        item.floorName,
        item.status,
      ]
        .filter((value) => value != null)
        .join(" ")
        .toLowerCase();

      return !query || searchableText.includes(query);
    });
  }, [
    allocations,
    invoices,
    eligibleAllocationIds,
    allocationSearch,
    invoiceForm.periodStart,
    invoiceForm.periodEnd,
  ]);

  const selectedInvoiceAllocation = useMemo(() => {
    return allocations.find(
      (item) =>
        String(item.id ?? item.allocationId) ===
        String(invoiceForm.allocationId)
    ) || null;
  }, [allocations, invoiceForm.allocationId]);

  const filteredHostlers = useMemo(() => {
    const query = search.trim().toLowerCase();

    return hostlers.filter((person) => {
      const matchesStatus =
        hostlerStatus === "ALL" ||
        statusValue(person.status) === hostlerStatus;

      const searchableValues = [
        person.fullName,
        person.email,
        person.phone,
        person.guardianName,
        person.guardianPhone,
        person.emergencyContact,
      ];

      const matchesSearch =
        !query ||
        searchableValues
          .filter(Boolean)
          .some((value) =>
            String(value).toLowerCase().includes(query)
          );

      return matchesStatus && matchesSearch;
    });
  }, [hostlers, search, hostlerStatus]);

  function openCreateHostler() {
    setEditingHostler(null);
    setHostlerForm({ ...EMPTY_HOSTLER });
    setShowHostlerForm(true);
    setError("");
    setNotice("");
  }

  function openEditHostler(person) {
    setEditingHostler(person);

    setHostlerForm({
      fullName: person.fullName || "",
      email: person.email || "",
      phone: person.phone || "",
      dateOfBirth: person.dateOfBirth || "",
      gender: person.gender || "",
      guardianName: person.guardianName || "",
      guardianPhone: person.guardianPhone || "",
      emergencyContact: person.emergencyContact || "",
      addressLine: person.addressLine || "",
      city: person.city || "",
      state: person.state || "",
      postalCode: person.postalCode || "",
      notes: person.notes || "",
    });

    setShowHostlerForm(true);
    setError("");
    setNotice("");
  }

  function closeHostlerForm() {
    setShowHostlerForm(false);
    setEditingHostler(null);
    setHostlerForm({ ...EMPTY_HOSTLER });
  }

  async function submitHostler(event) {
    event.preventDefault();

    if (!hostelId) {
      setError("Select a hostel first.");
      return;
    }

    if (!hostlerForm.fullName.trim() || !hostlerForm.email.trim()) {
      setError("Full name and email are required.");
      return;
    }

    setSaving(true);
    setError("");
    setNotice("");

    try {
      const payload = {
        ...hostlerForm,
        fullName: hostlerForm.fullName.trim(),
        email: hostlerForm.email.trim(),
        phone: hostlerForm.phone.trim() || null,
        dateOfBirth: hostlerForm.dateOfBirth || null,
        gender: hostlerForm.gender.trim() || null,
        guardianName: hostlerForm.guardianName.trim() || null,
        guardianPhone: hostlerForm.guardianPhone.trim() || null,
        emergencyContact: hostlerForm.emergencyContact.trim() || null,
        addressLine: hostlerForm.addressLine.trim() || null,
        city: hostlerForm.city.trim() || null,
        state: hostlerForm.state.trim() || null,
        postalCode: hostlerForm.postalCode.trim() || null,
        notes: hostlerForm.notes.trim() || null,
      };

      if (editingHostler) {
        await hostlerManagementService.updateHostler(
          hostelId,
          editingHostler.hostlerId,
          payload
        );

        setNotice("Hostler details updated successfully.");
      } else {
        await hostlerManagementService.createHostler(
          hostelId,
          payload
        );

        setNotice(
          "Hostler created successfully. Account activation will follow your configured backend workflow."
        );
      }

      closeHostlerForm();
      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function deactivateHostler(person) {
    const confirmed = window.confirm(
      `Remove ${person.fullName} from the active hostler list? Historical billing records should be preserved.`
    );

    if (!confirmed) return;

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await hostlerManagementService.deactivateHostler(
        hostelId,
        person.hostlerId
      );

      setNotice("Hostler deactivated successfully.");
      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function submitAllocation(event) {
    event.preventDefault();

    if (!hostelId) {
      setError("Select a hostel first.");
      return;
    }

    if (!allocationForm.hostlerId) {
      setError("Search for and select a hostler before allocating a bed.");
      return;
    }

    if (!allocationForm.bedId) {
      setError("Select an available bed before continuing.");
      return;
    }

    if (
      allocationForm.plannedCheckoutDate &&
      allocationForm.startDate &&
      allocationForm.plannedCheckoutDate < allocationForm.startDate
    ) {
      setError("Planned check-out cannot be before check-in.");
      return;
    }

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await hostlerManagementService.createAllocation(hostelId, {
        hostlerId: Number(allocationForm.hostlerId),
        bedId: Number(allocationForm.bedId),
        startDate: allocationForm.startDate,
        plannedCheckoutDate:
          allocationForm.plannedCheckoutDate || null,
      });

      setAllocationForm({ ...EMPTY_ALLOCATION });
      setAllocationHostlerSearch("");
      setAllocationHostlerPickerOpen(false);
      setNotice(
        "Bed reserved successfully. Check in the hostler when they arrive."
      );

      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function checkInAllocation(allocation) {
    if (allocation.startDate > today()) {
      setError("This reservation's check-in date is in the future.");
      return;
    }

    if (
      !window.confirm(
        `Check in ${allocation.hostlerName || "this hostler"}?`
      )
    ) {
      return;
    }

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await hostlerManagementService.checkIn(
        hostelId,
        allocation.id,
        today()
      );

      setNotice("Hostler checked in. The bed is now occupied.");
      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function checkOutAllocation(allocation) {
    if (
      !window.confirm(
        `Check out ${allocation.hostlerName || "this hostler"}?`
      )
    ) {
      return;
    }

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await hostlerManagementService.checkOut(
        hostelId,
        allocation.id,
        today()
      );

      setNotice(
        "Check-out completed. The bed is available for a new reservation."
      );

      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function cancelReservation(allocation) {
    if (!window.confirm("Cancel this bed reservation?")) return;

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await hostlerManagementService.cancelAllocation(
        hostelId,
        allocation.id
      );

      setNotice("Reservation cancelled.");
      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function submitInvoice(event) {
    event.preventDefault();

    if (!hostelId) {
      setError("Select a hostel first.");
      return;
    }

    if (!invoiceForm.allocationId) {
      setError("Search for and select an eligible allocation.");
      return;
    }

    if (
      invoiceForm.periodStart &&
      invoiceForm.periodEnd &&
      invoiceForm.periodEnd < invoiceForm.periodStart
    ) {
      setError("Billing period end cannot be before the start date.");
      return;
    }

    if (
      invoiceForm.periodStart &&
      invoiceForm.dueDate &&
      invoiceForm.dueDate < invoiceForm.periodStart
    ) {
      setError("Payment due date cannot be before the billing period starts.");
      return;
    }

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await hostlerManagementService.generateInvoice(hostelId, {
        allocationId: Number(invoiceForm.allocationId),
        periodStart: invoiceForm.periodStart,
        periodEnd: invoiceForm.periodEnd,
        dueDate: invoiceForm.dueDate,
      });

      setInvoiceForm({ ...EMPTY_INVOICE });
      setAllocationSearch("");
      setAllocationPickerOpen(false);
      setNotice("Invoice generated successfully.");

      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }
async function openPaymentProof(payment) {
  // Open the modal immediately so the user sees the loading state.
  setSelectedPaymentProof(payment);
  setProofPreviewUrl("");
  setProofPreviewContentType("");
  setProofPreviewError("");
  setProofPreviewLoading(true);

  try {
    const response = await hostlerManagementService.getPaymentProof(
      hostelId,
      payment.id
    );

    // Your service may return the Blob directly or an Axios response.
    let blob =
      response instanceof Blob
        ? response
        : response?.data instanceof Blob
          ? response.data
          : null;

    if (!blob || blob.size === 0) {
      throw new Error(
        "The server returned an empty payment proof."
      );
    }

    // Use the response's MIME type, falling back to payment metadata.
    let contentType = (
      blob.type ||
      payment.proofContentType ||
      ""
    )
      .split(";")[0]
      .trim()
      .toLowerCase();

    // If the server did not provide a MIME type, infer it from the filename.
    if (!contentType) {
      const filename = (
        payment.proofOriginalFilename || ""
      ).toLowerCase();

      if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
        contentType = "image/jpeg";
      } else if (filename.endsWith(".png")) {
        contentType = "image/png";
      } else if (filename.endsWith(".webp")) {
        contentType = "image/webp";
      } else if (filename.endsWith(".pdf")) {
        contentType = "application/pdf";
      }
    }

    const supportedTypes = [
      "image/jpeg",
      "image/png",
      "image/webp",
      "application/pdf",
    ];

    if (!supportedTypes.includes(contentType)) {
      throw new Error(
        `Unsupported preview format: ${contentType || "unknown"}. ` +
          "Please upload a JPG, PNG, WebP, or PDF file."
      );
    }

    // Ensure the Blob has the MIME type needed by the browser viewer.
    const previewBlob =
      blob.type.toLowerCase().split(";")[0].trim() === contentType
        ? blob
        : new Blob([blob], { type: contentType });

    const objectUrl = URL.createObjectURL(previewBlob);

    setProofPreviewContentType(contentType);
    setProofPreviewUrl(objectUrl);
  } catch (err) {
    console.error("Payment proof preview failed:", err);
    setProofPreviewError(getMessage(err));
  } finally {
    setProofPreviewLoading(false);
  }
}

function closePaymentProof() {
  if (proofPreviewUrl) {
    URL.revokeObjectURL(proofPreviewUrl);
  }

  setSelectedPaymentProof(null);
  setProofPreviewUrl("");
  setProofPreviewContentType("");
  setProofPreviewError("");
  setProofPreviewLoading(false);
}
  async function reviewPayment(payment, approve) {
    if (payment.method !== "MANUAL") {
      setError(
        "Only manual payments can be approved or rejected from this review action."
      );
      return;
    }

    if (payment.status !== "SUBMITTED") {
      setError("Only submitted manual payments can be reviewed.");
      return;
    }

    const decision = approve ? "approve" : "reject";

    if (
      !window.confirm(
        `Are you sure you want to ${decision} payment #${payment.id}?`
      )
    ) {
      return;
    }

    const note = window.prompt("Optional review note:", "");

    if (note === null) return;

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await hostlerManagementService.reviewPayment(
        hostelId,
        payment.id,
        approve,
        note.trim()
      );

      setNotice(
        approve
          ? "Payment approved. The backend should update the invoice balance."
          : "Payment rejected."
      );

      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  function handleHostlerField(field, value) {
    setHostlerForm((current) => ({
      ...current,
      [field]: value,
    }));
  }

  function handleAllocationField(field, value) {
    setAllocationForm((current) => ({
      ...current,
      [field]: value,
    }));
  }

  function handleInvoiceField(field, value) {
    setInvoiceForm((current) => ({
      ...current,
      [field]: value,
    }));
  }

  const activeHostlerCount = hostlers.filter(
    (item) => statusValue(item.status) === "ACTIVE"
  ).length;

  const activeStayCount = allocations.filter(
    (item) => statusValue(item.status) === "ACTIVE"
  ).length;

  const submittedManualPaymentCount = payments.filter(
    (item) =>
      statusValue(item.status) === "SUBMITTED" &&
      statusValue(item.method) === "MANUAL"
  ).length;

  if (loading && managedHostels.length === 0 && !hostelId) {
    return (
      <main className="hm-page">
        <div className="hm-loading">
          <RefreshCw className="hm-spin" size={20} />
          Loading assigned hostels...
        </div>
      </main>
    );
  }

  return (
    <main className="hm-page">
      <header className="hm-header">
        <div>
          <span className="hm-eyebrow">HOSTEL OPERATIONS</span>
          <h1>Hostler management</h1>
          <p>
            Manage hostlers, room allocations, stays, invoices and payments.
          </p>
        </div>

        <button
          className="hm-button light"
          type="button"
          onClick={() => {
            loadManagedHostels();
            loadWorkspace();
          }}
          disabled={loading || saving}
        >
          <RefreshCw
            size={16}
            className={loading ? "hm-spin" : ""}
          />
          Refresh
        </button>
      </header>

      {error && (
        <div className="hm-alert error" role="alert">
          <X size={18} />
          <span>{error}</span>
          <button
            className="hm-icon-button"
            type="button"
            onClick={() => setError("")}
            aria-label="Dismiss error"
          >
            <X size={16} />
          </button>
        </div>
      )}

      {notice && (
        <div className="hm-alert success" role="status">
          <CheckCircle2 size={18} />
          <span>{notice}</span>
          <button
            className="hm-icon-button"
            type="button"
            onClick={() => setNotice("")}
            aria-label="Dismiss notification"
          >
            <X size={16} />
          </button>
        </div>
      )}

      <section className="hm-section hm-hostel-select">
        <div>
          <label htmlFor="hm-hostel">Selected hostel</label>
          <select
            id="hm-hostel"
            value={hostelId}
            onChange={(event) => {
              setHostelId(event.target.value);
              setInvoiceForm({ ...EMPTY_INVOICE });
              setAllocationSearch("");
              setAllocationPickerOpen(false);
              setError("");
              setNotice("");
            }}
          >
            <option value="">Select a hostel</option>
            {managedHostels.map((hostel) => (
              <option
                key={hostel.hostelId}
                value={hostel.hostelId}
              >
                {hostel.hostelName} (
                {String(hostel.role || "").replaceAll("_", " ")})
              </option>
            ))}
          </select>
        </div>

        {selectedHostel && (
          <div className="hm-hostel-chip">
            <Building2 size={17} />
            {selectedHostel.hostelName}
          </div>
        )}
      </section>

      {!hostelId ? (
        <section className="hm-section">
          <EmptyState
            title="No assigned hostel"
            message="Ask the platform administrator to assign a hostel to your account."
          />
        </section>
      ) : (
        <>
          <section className="hm-stats">
            <article>
              <span className="hm-stat-icon purple">
                <Users size={20} />
              </span>
              <span className="hm-stat-label">Active hostlers</span>
              <strong>{activeHostlerCount}</strong>
            </article>

            <article>
              <span className="hm-stat-icon blue">
                <BedDouble size={20} />
              </span>
              <span className="hm-stat-label">Available beds</span>
              <strong>{availableBeds.length}</strong>
            </article>

            <article>
              <span className="hm-stat-icon green">
                <CalendarCheck size={20} />
              </span>
              <span className="hm-stat-label">Active stays</span>
              <strong>{activeStayCount}</strong>
            </article>

            <article>
              <span className="hm-stat-icon amber">
                <CreditCard size={20} />
              </span>
              <span className="hm-stat-label">Payments to review</span>
              <strong>{submittedManualPaymentCount}</strong>
            </article>
          </section>

          <nav className="hm-tabs" aria-label="Hostler operations">
            <button
              type="button"
              className={tab === "hostlers" ? "active" : ""}
              onClick={() => setTab("hostlers")}
            >
              <Users size={16} />
              Hostlers
            </button>

            <button
              type="button"
              className={tab === "allocations" ? "active" : ""}
              onClick={() => setTab("allocations")}
            >
              <BedDouble size={16} />
              Allocations
            </button>

            <button
              type="button"
              className={tab === "billing" ? "active" : ""}
              onClick={() => setTab("billing")}
            >
              <CreditCard size={16} />
              Billing & payments
            </button>
          </nav>

          {tab === "hostlers" && (
            <section className="hm-section">
              <div className="hm-section-heading">
                <div>
                  <h2>Hostler directory</h2>
                  <p>
                    Search and manage hostler records for this hostel.
                  </p>
                </div>

                <button
                  className="hm-button primary"
                  type="button"
                  onClick={openCreateHostler}
                  disabled={saving}
                >
                  <Plus size={17} />
                  Add hostler
                </button>
              </div>

              <div className="hm-toolbar">
                <div className="hm-search">
                  <Search size={17} />
                  <input
                    type="search"
                    placeholder="Search name, email, phone or guardian"
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                  />
                </div>

                <select
                  aria-label="Filter hostlers by status"
                  value={hostlerStatus}
                  onChange={(event) =>
                    setHostlerStatus(event.target.value)
                  }
                >
                  <option value="ALL">All hostlers</option>
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                </select>
              </div>

              {filteredHostlers.length === 0 ? (
                <EmptyState
                  title="No hostlers found"
                  message="Add a hostler or adjust the search and status filters."
                />
              ) : (
                <div className="hm-table-wrap">
                  <table className="hm-table">
                    <thead>
                      <tr>
                        <th>Hostler</th>
                        <th>Contact</th>
                        <th>Guardian</th>
                        <th>Status</th>
                        <th>Account</th>
                        <th>Actions</th>
                      </tr>
                    </thead>

                    <tbody>
                      {filteredHostlers.map((person) => (
                        <tr key={person.hostlerId}>
                          <td>
                            <strong>{person.fullName}</strong>
                            <span className="hm-subtext">
                              {person.gender || "Gender not specified"}
                            </span>
                          </td>

                          <td>
                            <span>{person.email}</span>
                            <span className="hm-subtext">
                              {person.phone || "No phone"}
                            </span>
                          </td>

                          <td>
                            <span>{person.guardianName || "—"}</span>
                            <span className="hm-subtext">
                              {person.guardianPhone || "—"}
                            </span>
                          </td>

                          <td>
                            <StatusPill status={person.status} />
                          </td>

                          <td>
                            <StatusPill status={person.accountStatus} />
                          </td>

                          <td>
                            <div className="hm-row-actions">
                              <button
                                className="hm-button small secondary"
                                type="button"
                                onClick={() => openEditHostler(person)}
                                disabled={saving}
                              >
                                Edit
                              </button>

                              {statusValue(person.status) === "ACTIVE" && (
                                <button
                                  className="hm-button small danger"
                                  type="button"
                                  onClick={() => deactivateHostler(person)}
                                  disabled={saving}
                                >
                                  Remove
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          )}

          {tab === "allocations" && (
            <>
              <section className="hm-section">
                <div className="hm-section-heading">
                  <div>
                    <h2>Allocate a bed</h2>
                    <p>
                      Only beds that are available and not reserved can be
                      selected.
                    </p>
                  </div>
                </div>

                <form
                  className="hm-form-grid"
                  onSubmit={submitAllocation}
                >
                  <label className="hm-searchable-hostler-field">
                    Hostler
                    <div className="hm-searchable-hostler-control">
                      <div className="hm-searchable-hostler-input-wrap">
                        <Search size={17} aria-hidden="true" />
                        <input
                          type="search"
                          role="combobox"
                          aria-label="Search and select hostler"
                          aria-autocomplete="list"
                          aria-expanded={allocationHostlerPickerOpen}
                          aria-controls="allocation-hostler-options"
                          autoComplete="off"
                          placeholder="Search hostler name or email..."
                          value={allocationHostlerSearch}
                          required={!allocationForm.hostlerId}
                          onFocus={() => {
                            setAllocationHostlerPickerOpen(true);
                            if (allocationForm.hostlerId) {
                              setAllocationHostlerSearch("");
                            }
                          }}
                          onChange={(event) => {
                            setAllocationHostlerSearch(event.target.value);
                            setAllocationHostlerPickerOpen(true);
                            if (allocationForm.hostlerId) {
                              handleAllocationField("hostlerId", "");
                            }
                          }}
                          onKeyDown={(event) => {
                            if (event.key === "Escape") {
                              setAllocationHostlerPickerOpen(false);
                            }
                          }}
                        />
                        {allocationHostlerSearch && (
                          <button
                            className="hm-searchable-hostler-clear"
                            type="button"
                            aria-label="Clear hostler search"
                            onClick={() => {
                              setAllocationHostlerSearch("");
                              handleAllocationField("hostlerId", "");
                              setAllocationHostlerPickerOpen(true);
                            }}
                          >
                            <X size={15} />
                          </button>
                        )}
                      </div>

                      {allocationHostlerPickerOpen && (
                        <div
                          className="hm-searchable-hostler-options"
                          id="allocation-hostler-options"
                          role="listbox"
                        >
                          {hostlers
                            .filter((person) => statusValue(person.status) === "ACTIVE")
                            .filter((person) => {
                              const query = allocationHostlerSearch.trim().toLowerCase();
                              if (!query) return true;
                              return [
                                person.fullName,
                                person.email,
                                person.phone,
                                person.hostlerId,
                              ].some((value) =>
                                String(value ?? "").toLowerCase().includes(query)
                              );
                            })
                            .map((person) => (
                              <button
                                className="hm-searchable-hostler-option"
                                key={person.hostlerId}
                                type="button"
                                role="option"
                                aria-selected={String(allocationForm.hostlerId) === String(person.hostlerId)}
                                onMouseDown={(event) => event.preventDefault()}
                                onClick={() => {
                                  handleAllocationField("hostlerId", String(person.hostlerId));
                                  setAllocationHostlerSearch(`${person.fullName} — ${person.email || `Hostler #${person.hostlerId}`}`);
                                  setAllocationHostlerPickerOpen(false);
                                }}
                              >
                                <strong>{person.fullName || `Hostler #${person.hostlerId}`}</strong>
                                <span>{person.email || "No email"}{person.phone ? ` · ${person.phone}` : ""}</span>
                              </button>
                            ))}

                          {hostlers.filter((person) => statusValue(person.status) === "ACTIVE").length === 0 && (
                            <div className="hm-searchable-hostler-empty">No active hostlers available.</div>
                          )}
                          {hostlers.some((person) => statusValue(person.status) === "ACTIVE") &&
                            !hostlers
                              .filter((person) => statusValue(person.status) === "ACTIVE")
                              .some((person) => [person.fullName, person.email, person.phone, person.hostlerId]
                                .some((value) => String(value ?? "").toLowerCase().includes(allocationHostlerSearch.trim().toLowerCase()))) && (
                              <div className="hm-searchable-hostler-empty">No hostlers match your search.</div>
                            )}
                        </div>
                      )}
                    </div>
                    {allocationForm.hostlerId && (
                      <span className="hm-subtext">Selected hostler: {hostlers.find((person) => String(person.hostlerId) === String(allocationForm.hostlerId))?.fullName || `#${allocationForm.hostlerId}`}</span>
                    )}
                  </label>

                  <label>
                    Available bed
                    <select
                      required
                      value={allocationForm.bedId}
                      onChange={(event) =>
                        handleAllocationField("bedId", event.target.value)
                      }
                    >
                      <option value="">Select available bed</option>
                      {availableBeds.map((bed) => (
                        <option key={bed.id} value={bed.id}>
                          {bed.bedNumber} — Room {bed.roomNumber}
                          {bed.flatName ? ` — ${bed.flatName}` : ""}
                          {bed.floorName ? ` — ${bed.floorName}` : ""}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label>
                    Planned check-in
                    <input
                      type="date"
                      required
                      min={today()}
                      value={allocationForm.startDate}
                      onChange={(event) =>
                        handleAllocationField(
                          "startDate",
                          event.target.value
                        )
                      }
                    />
                  </label>

                  <label>
                    Planned check-out
                    <input
                      type="date"
                      min={allocationForm.startDate || today()}
                      value={allocationForm.plannedCheckoutDate}
                      onChange={(event) =>
                        handleAllocationField(
                          "plannedCheckoutDate",
                          event.target.value
                        )
                      }
                    />
                  </label>

                  <div className="hm-form-actions">
                    <button
                      className="hm-button primary"
                      type="submit"
                      disabled={
                        saving ||
                        availableBeds.length === 0 ||
                        hostlers.every(
                          (item) =>
                            statusValue(item.status) !== "ACTIVE"
                        )
                      }
                    >
                      <Plus size={16} />
                      Reserve bed
                    </button>
                  </div>
                </form>

                {availableBeds.length === 0 && (
                  <p className="hm-inline-warning">
                    No beds are currently available in this hostel.
                  </p>
                )}
              </section>

              <section className="hm-section">
                <div className="hm-section-heading">
                  <div>
                    <h2>Allocation history</h2>
                    <p>
                      Manage reservations, active stays and check-outs.
                    </p>
                  </div>
                </div>

                {allocations.length === 0 ? (
                  <EmptyState
                    title="No allocations yet"
                    message="Reserve an available bed to start tracking a hostler's stay."
                  />
                ) : (
                  <div className="hm-table-wrap">
                    <table className="hm-table">
                      <thead>
                        <tr>
                          <th>Hostler</th>
                          <th>Bed</th>
                          <th>Check-in</th>
                          <th>Planned check-out</th>
                          <th>Actual check-out</th>
                          <th>Status</th>
                          <th>Actions</th>
                        </tr>
                      </thead>

                      <tbody>
                        {allocations.map((allocation) => (
                          <tr key={allocation.id}>
                            <td>
                              <strong>{allocation.hostlerName}</strong>
                              <span className="hm-subtext">
                                {allocation.hostlerEmail || "—"}
                              </span>
                            </td>

                            <td>
                              <strong>{allocation.bedNumber}</strong>
                              <span className="hm-subtext">
                                Room {allocation.roomNumber}
                              </span>
                            </td>

                            <td>{dateLabel(allocation.startDate)}</td>
                            <td>
                              {dateLabel(allocation.plannedCheckoutDate)}
                            </td>
                            <td>{dateLabel(allocation.endDate)}</td>

                            <td>
                              <StatusPill status={allocation.status} />
                            </td>

                            <td>
                              <div className="hm-row-actions">
                                {statusValue(allocation.status) ===
                                  "RESERVED" && (
                                  <>
                                    <button
                                      className="hm-button small primary"
                                      type="button"
                                      onClick={() =>
                                        checkInAllocation(allocation)
                                      }
                                      disabled={saving}
                                    >
                                      Check in
                                    </button>

                                    <button
                                      className="hm-button small danger"
                                      type="button"
                                      onClick={() =>
                                        cancelReservation(allocation)
                                      }
                                      disabled={saving}
                                    >
                                      Cancel
                                    </button>
                                  </>
                                )}

                                {statusValue(allocation.status) ===
                                  "ACTIVE" && (
                                  <button
                                    className="hm-button small primary"
                                    type="button"
                                    onClick={() =>
                                      checkOutAllocation(allocation)
                                    }
                                    disabled={saving}
                                  >
                                    Check out
                                  </button>
                                )}
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </section>
            </>
          )}

         

{tab === "billing" && (
  <>
    <section className="hm-section">
      <div className="hm-section-heading">
        <div>
          <h2>Generate invoice</h2>
          <p>
            Charges are calculated by the backend using the
            applicable room pricing and stay dates.
          </p>
        </div>
      </div>

      <form className="hm-form-grid" onSubmit={submitInvoice}>
        <label className="hm-invoice-allocation-field">
          Search allocation

          <input
            type="search"
            value={allocationSearch}
            onFocus={() => setAllocationPickerOpen(true)}
            onChange={(event) => {
              setAllocationSearch(event.target.value);
              handleInvoiceField("allocationId", "");
              setAllocationPickerOpen(true);
            }}
            placeholder="Type hostler, email, bed or allocation ID"
            autoComplete="off"
            aria-label="Search eligible allocations"
            aria-expanded={allocationPickerOpen}
          />

          {selectedInvoiceAllocation && (
            <div className="hm-selected-allocation">
              <span>
                Selected: <strong>
                  {selectedInvoiceAllocation.hostlerName ||
                    `Hostler #${selectedInvoiceAllocation.hostlerId}`}
                </strong>
                {" — Bed "}
                {selectedInvoiceAllocation.bedNumber ?? "—"}
                {" — Allocation #"}
                {selectedInvoiceAllocation.id ?? selectedInvoiceAllocation.allocationId}
              </span>
              <button
                type="button"
                className="hm-button small secondary"
                onClick={() => {
                  handleInvoiceField("allocationId", "");
                  setAllocationSearch("");
                  setAllocationPickerOpen(true);
                }}
              >
                Change
              </button>
            </div>
          )}

          {allocationPickerOpen && (
            <div className="hm-allocation-picker" role="listbox" aria-label="Eligible allocations">
              {!invoiceForm.periodStart || !invoiceForm.periodEnd ? (
                <div className="hm-allocation-picker-message">
                  Choose the billing period start and end dates first.
                </div>
              ) : invoiceForm.periodEnd < invoiceForm.periodStart ? (
                <div className="hm-allocation-picker-message">
                  Billing period end must be on or after the start date.
                </div>
              ) : eligibleAllocationsLoading ? (
                <div className="hm-allocation-picker-message">
                  Loading eligible allocations…
                </div>
              ) : eligibleAllocations.length === 0 ? (
                <div className="hm-allocation-picker-message">
                  No eligible allocations match this search and billing period.
                </div>
              ) : (
                eligibleAllocations.map((item) => {
                  const id = item.id ?? item.allocationId;
                  return (
                    <button
                      type="button"
                      role="option"
                      aria-selected={String(invoiceForm.allocationId) === String(id)}
                      className="hm-allocation-picker-option"
                      key={id}
                      onClick={() => {
                        handleInvoiceField("allocationId", String(id));
                        setAllocationSearch("");
                        setAllocationPickerOpen(false);
                      }}
                    >
                      <strong>
                        {item.hostlerName || `Hostler #${item.hostlerId ?? "—"}`}
                      </strong>
                      <span>
                        Bed {item.bedNumber ?? "—"} · Allocation #{id} · {item.status || "UNKNOWN"}
                      </span>
                      {item.hostlerEmail || item.email ? (
                        <span>{item.hostlerEmail || item.email}</span>
                      ) : null}
                    </button>
                  );
                })
              )}
              <button
                type="button"
                className="hm-allocation-picker-close"
                onClick={() => setAllocationPickerOpen(false)}
              >
                Close list
              </button>
            </div>
          )}

          {eligibleAllocationsError && (
            <span className="hm-subtext">
              Eligibility service unavailable. Showing allocations based on their current status.
            </span>
          )}

          {invoiceForm.periodStart &&
            invoiceForm.periodEnd &&
            !eligibleAllocationsLoading &&
            eligibleAllocations.length === 0 && (
              <span className="hm-subtext">
                No allocations are eligible for this billing
                period. Try another billing period or check
                whether invoices already exist.
              </span>
            )}
        </label>

        <label>
          Billing period starts
          <input
            type="date"
            required
            value={invoiceForm.periodStart}
            onChange={(event) => {
              setInvoiceForm((current) => ({
                ...current,
                periodStart: event.target.value,
                allocationId: "",
              }));
              setAllocationSearch("");
              setAllocationPickerOpen(false);
            }}
          />
        </label>

        <label>
          Billing period ends
          <input
            type="date"
            required
            min={invoiceForm.periodStart || undefined}
            value={invoiceForm.periodEnd}
            onChange={(event) => {
              setInvoiceForm((current) => ({
                ...current,
                periodEnd: event.target.value,
                allocationId: "",
              }));
              setAllocationSearch("");
              setAllocationPickerOpen(false);
            }}
          />
        </label>

        <label>
          Payment due date
          <input
            type="date"
            required
            value={invoiceForm.dueDate}
            onChange={(event) =>
              handleInvoiceField(
                "dueDate",
                event.target.value
              )
            }
          />
        </label>

        <div className="hm-form-actions">
          <button
            className="hm-button primary"
            type="submit"
            disabled={
              saving ||
              eligibleAllocationsLoading ||
              !invoiceForm.allocationId ||
              !eligibleAllocations.some(
                (item) =>
                  String(item.id) ===
                  String(invoiceForm.allocationId)
              ) ||
              !invoiceForm.periodStart ||
              !invoiceForm.periodEnd ||
              invoiceForm.periodEnd <
                invoiceForm.periodStart ||
              !invoiceForm.dueDate
            }
          >
            <FileText size={16} />
            Generate invoice
          </button>
        </div>
      </form>
    </section>

    <section className="hm-section">
      <div className="hm-section-heading">
        <div>
          <h2>Invoices</h2>
          <p>
            Review invoice totals, payments received and
            outstanding balances.
          </p>
        </div>
      </div>

      {invoices.length === 0 ? (
        <EmptyState
          title="No invoices"
          message="Generate an invoice for an eligible allocation."
        />
      ) : (
        <div className="hm-table-wrap">
          <table className="hm-table">
            <thead>
              <tr>
                <th>Invoice</th>
                <th>Hostler ID</th>
                <th>Billing period</th>
                <th>Due date</th>
                <th>Invoice total</th>
                <th>Amount paid</th>
                <th>Balance due</th>
                <th>Status</th>
              </tr>
            </thead>

            <tbody>
              {invoices.map((invoice) => {
                const total = getInvoiceTotal(invoice);
                const paid = Number(invoice.amountPaid ?? 0);
                const balance = getInvoiceBalance(invoice);

                return (
                  <tr key={invoice.id}>
                    <td>
                      <strong>#{invoice.id}</strong>
                      <span className="hm-subtext">
                        Allocation #
                        {invoice.allocationId ?? "—"}
                      </span>
                    </td>

                    <td>{getInvoiceHostlerId(invoice)}</td>

                    <td>
                      {dateLabel(invoice.periodStart)}
                      {" – "}
                      {dateLabel(invoice.periodEnd)}
                    </td>

                    <td>{dateLabel(invoice.dueDate)}</td>

                    <td>
                      <strong>
                        {money(total, invoice.currency)}
                      </strong>
                    </td>

                    <td>
                      {money(paid, invoice.currency)}
                    </td>

                    <td>
                      <strong>
                        {money(balance, invoice.currency)}
                      </strong>
                    </td>

                    <td>
                      <StatusPill status={invoice.status} />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  </>
)}


        </>
      )}

      {showHostlerForm && (
        <div
          className="hm-modal-overlay"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget && !saving) {
              closeHostlerForm();
            }
          }}
        >
          <section
            className="hm-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="hm-modal-title"
          >
            <header className="hm-modal-header">
              <div>
                <span className="hm-eyebrow">
                  {editingHostler ? "EDIT RECORD" : "NEW RECORD"}
                </span>
                <h2 id="hm-modal-title">
                  {editingHostler ? "Edit hostler" : "Add hostler"}
                </h2>
                <p>
                  Enter the hostler's contact and personal details.
                </p>
              </div>

              <button
                className="hm-icon-button"
                type="button"
                onClick={closeHostlerForm}
                disabled={saving}
                aria-label="Close hostler form"
              >
                <X size={18} />
              </button>
            </header>

            <form onSubmit={submitHostler}>
              <div className="hm-form-grid">
                <label>
                  Full name *
                  <input
                    required
                    maxLength={120}
                    value={hostlerForm.fullName}
                    onChange={(event) =>
                      handleHostlerField("fullName", event.target.value)
                    }
                  />
                </label>

                <label>
                  Email *
                  <input
                    type="email"
                    required
                    maxLength={254}
                    value={hostlerForm.email}
                    onChange={(event) =>
                      handleHostlerField("email", event.target.value)
                    }
                  />
                </label>

                <label>
                  Phone
                  <input
                    type="tel"
                    maxLength={30}
                    value={hostlerForm.phone}
                    onChange={(event) =>
                      handleHostlerField("phone", event.target.value)
                    }
                  />
                </label>

                <label>
                  Date of birth
                  <input
                    type="date"
                    max={today()}
                    value={hostlerForm.dateOfBirth}
                    onChange={(event) =>
                      handleHostlerField(
                        "dateOfBirth",
                        event.target.value
                      )
                    }
                  />
                </label>

                <label>
                  Gender
                  <select
                    value={hostlerForm.gender}
                    onChange={(event) =>
                      handleHostlerField("gender", event.target.value)
                    }
                  >
                    <option value="">Not specified</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                    <option value="PREFER_NOT_TO_SAY">
                      Prefer not to say
                    </option>
                  </select>
                </label>

                <label>
                  Guardian name
                  <input
                    maxLength={150}
                    value={hostlerForm.guardianName}
                    onChange={(event) =>
                      handleHostlerField(
                        "guardianName",
                        event.target.value
                      )
                    }
                  />
                </label>

                <label>
                  Guardian phone
                  <input
                    type="tel"
                    maxLength={20}
                    value={hostlerForm.guardianPhone}
                    onChange={(event) =>
                      handleHostlerField(
                        "guardianPhone",
                        event.target.value
                      )
                    }
                  />
                </label>

                <label>
                  Emergency contact
                  <input
                    type="tel"
                    maxLength={20}
                    value={hostlerForm.emergencyContact}
                    onChange={(event) =>
                      handleHostlerField(
                        "emergencyContact",
                        event.target.value
                      )
                    }
                  />
                </label>

                <label className="hm-full-width">
                  Address
                  <input
                    maxLength={255}
                    value={hostlerForm.addressLine}
                    onChange={(event) =>
                      handleHostlerField(
                        "addressLine",
                        event.target.value
                      )
                    }
                  />
                </label>

                <label>
                  City
                  <input
                    maxLength={100}
                    value={hostlerForm.city}
                    onChange={(event) =>
                      handleHostlerField("city", event.target.value)
                    }
                  />
                </label>

                <label>
                  State
                  <input
                    maxLength={100}
                    value={hostlerForm.state}
                    onChange={(event) =>
                      handleHostlerField("state", event.target.value)
                    }
                  />
                </label>

                <label>
                  Postal code
                  <input
                    maxLength={20}
                    value={hostlerForm.postalCode}
                    onChange={(event) =>
                      handleHostlerField(
                        "postalCode",
                        event.target.value
                      )
                    }
                  />
                </label>

                <label className="hm-full-width">
                  Notes
                  <textarea
                    rows={3}
                    maxLength={2000}
                    value={hostlerForm.notes}
                    onChange={(event) =>
                      handleHostlerField("notes", event.target.value)
                    }
                  />
                </label>
              </div>

              <footer className="hm-form-actions">
                <button
                  className="hm-button secondary"
                  type="button"
                  onClick={closeHostlerForm}
                  disabled={saving}
                >
                  Cancel
                </button>

                <button
                  className="hm-button primary"
                  type="submit"
                  disabled={saving}
                >
                  {saving
                    ? "Saving..."
                    : editingHostler
                      ? "Save changes"
                      : "Create hostler"}
                </button>
              </footer>
            </form>
          </section>
        </div>
      )}
      {/* Payment Proof Preview Modal */}
{selectedPaymentProof && (
  <div
    className="hm-modal-overlay hm-proof-overlay"
    role="presentation"
    onMouseDown={(event) => {
      if (event.target === event.currentTarget) {
        closePaymentProof();
      }
    }}
  >
    <section
      className="hm-modal hm-proof-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="hm-proof-title"
    >
      <header className="hm-modal-header">
        <div>
          <span className="hm-eyebrow">PAYMENT VERIFICATION</span>

          <h2 id="hm-proof-title">Payment proof</h2>

          <p>
            Payment #{selectedPaymentProof.id}
            {" · "}
            {money(
              selectedPaymentProof.amount,
              selectedPaymentProof.currency
            )}
          </p>

          {selectedPaymentProof.proofOriginalFilename && (
            <span className="hm-subtext">
              {selectedPaymentProof.proofOriginalFilename}
            </span>
          )}
        </div>

        <button
          className="hm-icon-button"
          type="button"
          onClick={closePaymentProof}
          aria-label="Close payment proof"
        >
          <X size={18} />
        </button>
      </header>

      <div className="hm-proof-body">
        {proofPreviewLoading && (
          <div className="hm-proof-message">
            <RefreshCw className="hm-spin" size={24} />
            <p>Loading payment proof...</p>
          </div>
        )}

        {!proofPreviewLoading && proofPreviewError && (
          <div className="hm-proof-message error" role="alert">
            <X size={24} />
            <strong>Unable to preview payment proof</strong>
            <p>{proofPreviewError}</p>

            <button
              className="hm-button secondary"
              type="button"
              onClick={() => openPaymentProof(selectedPaymentProof)}
            >
              Try again
            </button>
          </div>
        )}

        {!proofPreviewLoading &&
          !proofPreviewError &&
          proofPreviewUrl &&
          proofPreviewContentType.startsWith("image/") && (
            <div className="hm-proof-image-wrap">
              <img
                className="hm-proof-image"
                src={proofPreviewUrl}
                alt={`Payment proof for payment ${selectedPaymentProof.id}`}
              />
            </div>
          )}

        {!proofPreviewLoading &&
          !proofPreviewError &&
          proofPreviewUrl &&
          proofPreviewContentType === "application/pdf" && (
            <iframe
              className="hm-proof-pdf"
              src={proofPreviewUrl}
              title={`Payment proof PDF for payment ${selectedPaymentProof.id}`}
            />
          )}
      </div>

      <footer className="hm-form-actions hm-proof-footer">
        {proofPreviewUrl && (
          <a
            className="hm-button secondary"
            href={proofPreviewUrl}
            target="_blank"
            rel="noopener noreferrer"
          >
            Open in new tab
          </a>
        )}

        <button
          className="hm-button primary"
          type="button"
          onClick={closePaymentProof}
        >
          Close preview
        </button>
      </footer>
    </section>
  </div>
)}
    </main>
  );
}


