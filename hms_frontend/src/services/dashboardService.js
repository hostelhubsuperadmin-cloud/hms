
import axiosClient from "../api/axiosClient";

export const dashboardService = {
  getSuperAdminHostels() {
    return axiosClient.get("/api/super-admin/hostels");
  },

  getSuperAdminApplications() {
    return axiosClient.get("/api/super-admin/hostler-applications");
  },

  getHostelAdminHostels() {
    return axiosClient.get("/api/hostel-admin/my-hostels");
  },

  getMyApplications() {
    return axiosClient.get("/api/hostler-applications/my");
  },

  getMyPayments() {
    return axiosClient.get("/api/payments/mine");
  },

  getMyInvoices() {
    return axiosClient.get("/api/billing/invoices/mine");
  },

  getNotifications() {
    return axiosClient.get("/api/notifications");
  },
};
