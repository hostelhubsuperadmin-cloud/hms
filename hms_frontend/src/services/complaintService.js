import axiosClient from "../api/axiosClient";

const complaintService = {
  async getMyComplaints() {
    const { data } = await axiosClient.get("/api/hostler/complaints");
    return data;
  },

  async getMyComplaint(id) {
    const { data } = await axiosClient.get(
      `/api/hostler/complaints/${encodeURIComponent(id)}`
    );
    return data;
  },

  async createComplaint(payload) {
    const { data } = await axiosClient.post(
      "/api/hostler/complaints",
      payload
    );
    return data;
  },

  async getHostelComplaints(hostelId, status = "ALL") {
  if (
    hostelId === undefined ||
    hostelId === null ||
    String(hostelId).trim() === "" ||
    String(hostelId) === "undefined" ||
    String(hostelId) === "null"
  ) {
    throw new Error("A valid hostel ID is required to load complaints.");
  }

  const { data } = await axiosClient.get(
    `/api/hostel-admin/hostels/${encodeURIComponent(hostelId)}/complaints`,
    { params: { status } }
  );

  return data;
},

 async updateComplaintStatus(hostelId, complaintId, payload) {
  if (
    hostelId === undefined ||
    hostelId === null ||
    String(hostelId).trim() === "" ||
    String(hostelId) === "undefined" ||
    String(hostelId) === "null"
  ) {
    throw new Error("A valid hostel ID is required to update a complaint.");
  }

  if (
    complaintId === undefined ||
    complaintId === null ||
    String(complaintId).trim() === "" ||
    String(complaintId) === "undefined" ||
    String(complaintId) === "null"
  ) {
    throw new Error("A valid complaint ID is required.");
  }

  const { data } = await axiosClient.patch(
    `/api/hostel-admin/hostels/${encodeURIComponent(hostelId)}/complaints/${encodeURIComponent(complaintId)}`,
    payload
  );

  return data;
},
};

export default complaintService;