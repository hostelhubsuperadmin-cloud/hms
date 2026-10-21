import axiosClient, {
  clearCsrfToken,
  initializeCsrf,
} from "../api/axiosClient";

const authService = {
  initializeCsrf() {
    return initializeCsrf();
  },

  async login(credentials) {
    await initializeCsrf();

    const { data } = await axiosClient.post(
      "/api/auth/login",
      {
        email: credentials.email.trim(),
        password: credentials.password,
      }
    );

    return data;
  },

  async register(payload) {
    await initializeCsrf();

    const { data } = await axiosClient.post(
      "/api/auth/register",
      {
        fullName: payload.fullName.trim(),
        email: payload.email.trim(),
        password: payload.password,
        phone: payload.phone?.trim() || null,
      }
    );

    return data;
  },

  async getCurrentUser() {
    const { data } = await axiosClient.get("/api/auth/me");
    return data;
  },

  async logout() {
    try {
      await initializeCsrf();
      await axiosClient.post("/api/auth/logout");
    } finally {
      clearCsrfToken();
    }
  },

  async activateAccount(token) {
    const { data } = await axiosClient.get(
      "/api/auth/activate",
      { params: { token } }
    );

    return data;
  },
};

export default authService;