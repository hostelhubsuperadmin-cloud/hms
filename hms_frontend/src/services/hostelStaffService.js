
import axiosClient, { initializeCsrf } from "../api/axiosClient";

function requireHostelId(hostelId) {
  if (hostelId === undefined || hostelId === null || hostelId === "") {
    throw new Error("Hostel ID is required.");
  }
}

function requireStaffId(staffId) {
  if (staffId === undefined || staffId === null || staffId === "") {
    throw new Error("Staff ID is missing. Please refresh the staff list and try again.");
  }
}

function normalizeStaffPayload(payload, includeEmail = false) {
  const result = {
    fullName: payload.fullName?.trim(),
    phone: payload.phone?.trim() || null,
    role: payload.role,
  };

  if (includeEmail) {
    result.email = payload.email?.trim();
  }

  if (!result.fullName) {
    throw new Error("Full name is required.");
  }

  if (includeEmail && !result.email) {
    throw new Error("Email is required.");
  }

  if (!result.role) {
    throw new Error("Please select a staff role.");
  }

  return result;
}

const hostelStaffService = {
  async getStaff(hostelId) {
    requireHostelId(hostelId);

    const { data } = await axiosClient.get(
      `/api/hostel-admin/hostels/${encodeURIComponent(hostelId)}/staff`
    );

    return data;
  },

  async createStaff(hostelId, payload) {
    requireHostelId(hostelId);

    await initializeCsrf();

    const body = normalizeStaffPayload(payload, true);

    const { data } = await axiosClient.post(
      `/api/hostel-admin/hostels/${encodeURIComponent(hostelId)}/staff`,
      body
    );

    return data;
  },

  async updateStaff(hostelId, staffId, payload) {
    requireHostelId(hostelId);
    requireStaffId(staffId);

    await initializeCsrf();

    const body = normalizeStaffPayload(payload, false);

    const { data } = await axiosClient.put(
      `/api/hostel-admin/hostels/${encodeURIComponent(hostelId)}/staff/${encodeURIComponent(staffId)}`,
      body
    );

    return data;
  },

  async removeStaff(hostelId, staffId) {
    requireHostelId(hostelId);
    requireStaffId(staffId);

    await initializeCsrf();

    await axiosClient.delete(
      `/api/hostel-admin/hostels/${encodeURIComponent(hostelId)}/staff/${encodeURIComponent(staffId)}`
    );

    return true;
  },
};

export default hostelStaffService;

