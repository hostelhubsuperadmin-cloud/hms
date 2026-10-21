import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./AuthContext";
import {
  getDashboardPath,
  hasAnyRole,
} from "../utils/roles";

export default function RoleRoute({ allowedRoles = [] }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="auth-page">
        <p role="status">Checking permissions...</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (!hasAnyRole(user, allowedRoles)) {
    return (
      <Navigate
        to={getDashboardPath(user)}
        replace
      />
    );
  }

  return <Outlet />;
}