
import axiosClient, { initializeCsrf } from "../api/axiosClient";

const paymentService = {
  async getMyInvoices() {
    const { data } = await axiosClient.get("/api/billing/invoices/mine");
    return data;
  },

  async getMyPayments() {
    const { data } = await axiosClient.get("/api/payments/mine");
    return data;
  },

  async submitManualPayment({
    invoiceId,
    amount,
    manualReference,
  }) {
    await initializeCsrf();

    const { data } = await axiosClient.post("/api/payments/manual", {
      invoiceId: Number(invoiceId),
      amount: Number(amount),
      idempotencyKey: createIdempotencyKey("manual"),
      manualReference: manualReference?.trim() || null,
    });

    return data;
  },

  async uploadProof(paymentId, file) {
    await initializeCsrf();

    const formData = new FormData();
    formData.append("file", file);

    const { data } = await axiosClient.post(
      `/api/payments/${encodeURIComponent(paymentId)}/proof`,
      formData
    );

    return data;
  },

  async createRazorpayOrder({ invoiceId, amount }) {
    await initializeCsrf();

    const { data } = await axiosClient.post(
      "/api/payments/razorpay/order",
      {
        invoiceId: Number(invoiceId),
        amount: Number(amount),
        idempotencyKey: createIdempotencyKey("razorpay"),
      }
    );

    return data;
  },

  async verifyRazorpayPayment({
    razorpayOrderId,
    razorpayPaymentId,
    razorpaySignature,
  }) {
    await initializeCsrf();

    const { data } = await axiosClient.post(
      "/api/payments/razorpay/verify",
      {
        razorpayOrderId,
        razorpayPaymentId,
        razorpaySignature,
      }
    );

    return data;
  },
};

function createIdempotencyKey(prefix) {
  let uuid;

  if (
    typeof crypto !== "undefined" &&
    typeof crypto.randomUUID === "function"
  ) {
    uuid = crypto.randomUUID();
  }

  if (uuid) {
    return `${prefix}-${uuid}`;
  }

  return `${prefix}-${Date.now()}-${Math.random()
    .toString(36)
    .slice(2)}`;
}

export default paymentService;

