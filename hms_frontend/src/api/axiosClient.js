
import axios from "axios";

const axiosClient = axios.create({
  baseURL: (
    import.meta.env.VITE_API_BASE_URL || "http://localhost:9999"
  ).replace(/\/+$/, ""),
  withCredentials: true,
  headers: {
    Accept: "application/json",
  },
});

const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS", "TRACE"]);

let csrfPromise = null;

function readCookie(name) {
  if (typeof document === "undefined") {
    return null;
  }

  const prefix = `${name}=`;
  const cookie = document.cookie
    .split(";")
    .map((item) => item.trim())
    .find((item) => item.startsWith(prefix));

  if (!cookie) {
    return null;
  }

  const value = cookie.slice(prefix.length);

  try {
    return decodeURIComponent(value);
  } catch {
    return value;
  }
}

export function clearCsrfToken() {
  csrfPromise = null;
}

/**
 * Ask Spring Security for a current CSRF token.
 * The backend should set the XSRF-TOKEN cookie.
 */
export async function initializeCsrf(force = false) {
  const existingToken = readCookie("XSRF-TOKEN");

  if (existingToken && !force) {
    return existingToken;
  }

  if (csrfPromise && !force) {
    return csrfPromise;
  }

  csrfPromise = axiosClient
    .get("/api/auth/csrf")
    .then(({ data }) => {
      const cookieToken = readCookie("XSRF-TOKEN");
      const token = cookieToken || data?.token;

      if (!token) {
        throw new Error(
          "CSRF token missing. Check that /api/auth/csrf sets the XSRF-TOKEN cookie."
        );
      }

      return token;
    })
    .finally(() => {
      csrfPromise = null;
    });

  return csrfPromise;
}

axiosClient.interceptors.request.use(async (config) => {
  const method = (config.method || "GET").toUpperCase();

  if (!SAFE_METHODS.has(method)) {
    // Read the cookie immediately before the write request.
    // If absent, request a fresh token from Spring Security.
    let token = readCookie("XSRF-TOKEN");

    if (!token) {
      token = await initializeCsrf(true);
    }

    config.headers = config.headers || {};
    config.headers["X-XSRF-TOKEN"] = token;
  }

  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 403) {
      const method = (
        error.config?.method || "GET"
      ).toUpperCase();

      if (!SAFE_METHODS.has(method)) {
        clearCsrfToken();
      }
    }

    return Promise.reject(error);
  }
);

export function getApiError(error) {
  const data = error?.response?.data;

  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data?.detail) {
    return data.detail;
  }

  if (data?.message) {
    return data.message;
  }

  if (data?.error) {
    return data.error;
  }

  if (error?.response?.status === 401) {
    return "Your session may have expired. Please log in again.";
  }

  if (error?.response?.status === 403) {
    return "Request forbidden. Check your login session, CSRF token, and permissions.";
  }

  if (error?.response?.status === 400) {
    return "Invalid request data. Check the submitted fields.";
  }

  if (!error?.response) {
    return "Cannot connect to the backend. Check that the API server is running.";
  }

  return error.message || "An unexpected error occurred.";
}

export default axiosClient;

