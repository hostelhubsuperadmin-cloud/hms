
import axiosClient, { initializeCsrf } from "../api/axiosClient";

const getStaffUrl = (hostelId) => {
  if (!hostelId) {
    throw new Error("Hostel ID is required.");
  }

  return `/api/hostel-admin/hostels/${hostelId}/staff`;
};

const hostelStaffService = {
  async getStaff(hostelId) {
    const response = await axiosClient.get(getStaffUrl(hostelId));
    return response.data;
  },

  async addStaff(hostelId, staffData) {
    await initializeCsrf();

    const response = await axiosClient.post(
      getStaffUrl(hostelId),
      staffData
    );

    return response.data;
  },

  async updateStaff(hostelId, staffId, staffData) {
    if (!staffId) {
      throw new Error("Staff ID is required to update a team member.");
    }

    await initializeCsrf();

    const response = await axiosClient.put(
      `${getStaffUrl(hostelId)}/${staffId}`,
      staffData
    );

    return response.data;
  },

  async removeStaff(hostelId, staffId) {
    if (!staffId) {
      throw new Error("Staff ID is required to remove a team member.");
    }

    await initializeCsrf();

    const response = await axiosClient.delete(
      `${getStaffUrl(hostelId)}/${staffId}`
    );

    return response.data;
  },
};

export default hostelStaffService;

