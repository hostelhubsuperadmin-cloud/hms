
import axiosClient from "../api/axiosClient";

const BASE = "/api/super-admin";

const superAdminService = {
  // Hostel management
  getHostels() {
    return axiosClient.get(`${BASE}/hostels`);
  },

  getHostel(id) {
    return axiosClient.get(`${BASE}/hostels/${id}`);
  },

  createHostel(payload) {
    return axiosClient.post(`${BASE}/hostels`, payload);
  },

  updateHostel(id, payload) {
    return axiosClient.put(`${BASE}/hostels/${id}`, payload);
  },

  updateHostelStatus(id, status) {
    return axiosClient.patch(
      `${BASE}/hostels/${id}/status`,
      null,
      { params: { status } }
    );
  },

  // Hostel Admin management
  getHostelAdmins() {
    return axiosClient.get(`${BASE}/hostel-admins`);
  },

  createHostelAdmin(payload) {
    return axiosClient.post(`${BASE}/hostel-admins`, payload);
  },

  assignAdmin(adminId, hostelId) {
    return axiosClient.post(
      `${BASE}/hostel-admins/${adminId}/assign/${hostelId}`
    );
  },

  // Hostler application review
  getApplications(status) {
    return axiosClient.get(`${BASE}/hostler-applications`, {
      params: status && status !== "ALL" ? { status } : {},
    });
  },

  getApplication(id) {
    return axiosClient.get(`${BASE}/hostler-applications/${id}`);
  },

  reviewApplication(id, payload) {
    return axiosClient.patch(
      `${BASE}/hostler-applications/${id}/review`,
      payload
    );
  },
  deleteHostelAdmin(adminId) {
  if (!adminId) {
    throw new Error("Hostel Admin ID is required.");
  }

  return axiosClient.delete(
    `${BASE}/hostel-admins/${adminId}`
  );
},
  getAdminAssignments(adminId) {
    return axiosClient.get(
      `${BASE}/hostel-admins/${adminId}/assignments`
    );
  },

  unassignAdminFromHostel(adminId, hostelId) {
    return axiosClient.delete(
      `${BASE}/hostel-admins/${adminId}/assign/${hostelId}`
    );
  },
};

export default superAdminService;
