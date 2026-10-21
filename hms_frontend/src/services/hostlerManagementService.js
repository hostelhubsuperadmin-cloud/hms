import axiosClient, { initializeCsrf } from "../api/axiosClient";

function requireHostelId(hostelId) {
  if (hostelId === undefined || hostelId === null || hostelId === "") {
    throw new Error("Please select a hostel.");
  }
}

const hostlerManagementService = {
  async getManagedHostels() {
    const { data } = await axiosClient.get("/api/managed-hostels");
    return data;
  },

  async getHostlers(hostelId) {
    requireHostelId(hostelId);

    const { data } = await axiosClient.get(
      `/api/hostels/${encodeURIComponent(hostelId)}/hostlers`
    );

    return data;
  },

  async createHostler(hostelId, payload) {
    requireHostelId(hostelId);
    await initializeCsrf();

    const { data } = await axiosClient.post(
      `/api/hostels/${encodeURIComponent(hostelId)}/hostlers`,
      payload
    );

    return data;
  },

  async updateHostler(hostelId, hostlerId, payload) {
    requireHostelId(hostelId);
    await initializeCsrf();

    const { data } = await axiosClient.put(
      `/api/hostels/${encodeURIComponent(hostelId)}/hostlers/${encodeURIComponent(hostlerId)}`,
      payload
    );

    return data;
  },

  async deactivateHostler(hostelId, hostlerId) {
    requireHostelId(hostelId);
    await initializeCsrf();

    await axiosClient.delete(
      `/api/hostels/${encodeURIComponent(hostelId)}/hostlers/${encodeURIComponent(hostlerId)}`
    );
  },

  async getAvailableBeds(hostelId) {
    requireHostelId(hostelId);

    const { data } = await axiosClient.get(
      `/api/hostels/${encodeURIComponent(hostelId)}/available-beds`
    );

    return data;
  },

  async getAllocations(hostelId) {
    requireHostelId(hostelId);

    const { data } = await axiosClient.get(
      `/api/hostels/${encodeURIComponent(hostelId)}/allocations`
    );

    return data;
  },

  async createAllocation(hostelId, payload) {
    requireHostelId(hostelId);
    await initializeCsrf();

    const { data } = await axiosClient.post(
      `/api/hostels/${encodeURIComponent(hostelId)}/allocations`,
      payload
    );

    return data;
  },

  async checkIn(hostelId, allocationId, checkInDate) {
    await initializeCsrf();

    const { data } = await axiosClient.patch(
      `/api/hostels/${encodeURIComponent(hostelId)}/allocations/${encodeURIComponent(allocationId)}/check-in`,
      { checkInDate }
    );

    return data;
  },

  async checkOut(hostelId, allocationId, checkOutDate) {
    await initializeCsrf();

    const { data } = await axiosClient.patch(
      `/api/hostels/${encodeURIComponent(hostelId)}/allocations/${encodeURIComponent(allocationId)}/check-out`,
      { checkOutDate }
    );

    return data;
  },

  async cancelAllocation(hostelId, allocationId) {
    await initializeCsrf();

    await axiosClient.delete(
      `/api/hostels/${encodeURIComponent(hostelId)}/allocations/${encodeURIComponent(allocationId)}`
    );
  },

  async getInvoices(hostelId, status) {
    requireHostelId(hostelId);

    const { data } = await axiosClient.get(
      `/api/hostels/${encodeURIComponent(hostelId)}/billing/invoices`,
      { params: status ? { status } : {} }
    );

    return data;
  },
  async getEligibleInvoiceAllocationIds(
  hostelId,
  periodStart,
  periodEnd
) {
  requireHostelId(hostelId);

  const { data } = await axiosClient.get(
    `/api/hostels/${encodeURIComponent(hostelId)}/billing/invoices/eligible-allocations`,
    {
      params: {
        periodStart,
        periodEnd,
      },
    }
  );

  return Array.isArray(data) ? data : [];
},

  async generateInvoice(hostelId, payload) {
    requireHostelId(hostelId);
    await initializeCsrf();

    const { data } = await axiosClient.post(
      `/api/hostels/${encodeURIComponent(hostelId)}/billing/invoices/generate`,
      payload
    );

    return data;
  },

  async getPayments(hostelId, status) {
    requireHostelId(hostelId);

    const { data } = await axiosClient.get(
      `/api/hostels/${encodeURIComponent(hostelId)}/billing/payments`,
      { params: status && status !== "ALL" ? { status } : {} }
    );

    return data;
  },

  async reviewPayment(hostelId, paymentId, approve, note) {
    requireHostelId(hostelId);
    await initializeCsrf();

    const { data } = await axiosClient.patch(
      `/api/hostels/${encodeURIComponent(hostelId)}/billing/payments/${encodeURIComponent(paymentId)}/review`,
      { approve, note }
    );

    return data;
  },
  
async getPaymentProof(hostelId, paymentId) {
  requireHostelId(hostelId);

  const response = await axiosClient.get(
    `/api/hostels/${encodeURIComponent(hostelId)}/billing/payments/${encodeURIComponent(paymentId)}/proof`,
    {
      responseType: "blob",
    }
  );

  return response.data;
},
  
};

export default hostlerManagementService;