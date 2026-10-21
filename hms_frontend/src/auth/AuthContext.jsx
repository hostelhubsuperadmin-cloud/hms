
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";

import axiosClient, {
  initializeCsrf,
  clearCsrfToken,
} from "../api/axiosClient";

import authService from "../services/authService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Refresh the currently authenticated user.
  const refreshUser = useCallback(async () => {
    try {
      const { data } = await axiosClient.get("/api/auth/me");
      const currentUser = data?.user ?? data;

      setUser(currentUser);
      return currentUser;
    } catch (error) {
      if (
        error.response?.status === 401 ||
        error.response?.status === 403
      ) {
        setUser(null);
        return null;
      }

      throw error;
    }
  }, []);

  // Initialize CSRF and check whether a user is already logged in.
  useEffect(() => {
    let active = true;

    async function initializeAuth() {
      try {
        await axiosClient.get("/api/auth/csrf");

        const { data } = await axiosClient.get("/api/auth/me");

        if (active) {
          setUser(data?.user ?? data);
        }
      } catch (error) {
        if (active) {
          setUser(null);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    initializeAuth();

    return () => {
      active = false;
    };
  }, []);

  // Register a new account.
  const register = useCallback(async (payload) => {
    await initializeCsrf(true);

    return await authService.register(payload);
  }, []);

  // Log in an existing account.
  const login = useCallback(async (credentials) => {
    await initializeCsrf(true);

    await axiosClient.post("/api/auth/login", credentials);

    const { data } = await axiosClient.get("/api/auth/me");
    const currentUser = data?.user ?? data;

    setUser(currentUser);
    return currentUser;
  }, []);

  // Log out the current user.
  const logout = useCallback(async () => {
    await axiosClient.get("/api/auth/csrf");

    await axiosClient.post("/api/auth/logout");

    setUser(null);
    clearCsrfToken();
  }, []);

  // Expose authentication functions to components.
  const value = {
    user,
    loading,
    isAuthenticated: Boolean(user),
    login,
    register,
    logout,
    refreshUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return context;
}


