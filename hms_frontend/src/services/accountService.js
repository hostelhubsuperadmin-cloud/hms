
import axiosClient, { initializeCsrf } from "../api/axiosClient";

const accountService = {
  async activateAccount({ token, newPassword, confirmPassword }) {
    await initializeCsrf();
    const { data } = await axiosClient.post("/api/auth/activate", {
      token,
      newPassword,
      confirmPassword,
    });
    return data;
  },

  async resendActivation(email) {
    await initializeCsrf();
    const { data } = await axiosClient.post("/api/auth/resend-activation", {
      email: email.trim(),
    });
    return data;
  },

  async requestPasswordOtp(email) {
    await initializeCsrf();
    const { data } = await axiosClient.post("/api/auth/forgot-password", {
      email: email.trim(),
    });
    return data;
  },

  async verifyPasswordOtp({ email, otp }) {
    await initializeCsrf();
    const { data } = await axiosClient.post("/api/auth/verify-reset-otp", {
      email: email.trim(),
      otp: otp.trim(),
    });
    return data;
  },

  async resetPassword({ resetToken, newPassword, confirmPassword }) {
    await initializeCsrf();
    const { data } = await axiosClient.post("/api/auth/reset-password", {
      resetToken,
      newPassword,
      confirmPassword,
    });
    return data;
  },

  async changePassword({ currentPassword, newPassword, confirmPassword }) {
    await initializeCsrf();
    const { data } = await axiosClient.post("/api/auth/change-password", {
      currentPassword,
      newPassword,
      confirmPassword,
    });
    return data;
  },
};

export default accountService;
