import HostlerPaymentsPage from "../pages/hostel-admin/HostlerPaymentsPage";
import PublicHostelPage from "../pages/public/PublicHostelPage";
import { Navigate, Route, Routes } from "react-router-dom";
import HostlerManagementPage from "../pages/hostel-admin/HostlerManagementPage";
import { useAuth } from "../auth/AuthContext";
import ProtectedRoute from "../auth/ProtectedRoute";
import RoleRoute from "../auth/RoleRoute";
import HostlerComplaintsPage from "../pages/hostler/HostlerComplaintsPage";
import ComplaintsManagementPage from "../pages/hostel-admin/ComplaintsManagementPage";
import LoginPage from "../pages/auth/LoginPage";
import RegisterPage from "../pages/auth/RegisterPage";

import RoleDashboardPage from "../pages/dashboard/RoleDashboardPage";
import SuperAdminManagementPage from "../pages/super-admin/SuperAdminManagementPage";
import HostelAdminWorkspacePage from "../pages/hostel-admin/HostelAdminWorkspacePage";
import HostelStaffPage from "../pages/hostel-admin/HostelStaffPage";

import {
  ActivateAccountPage,
  ActivationExpiredPage,
  ActivationSuccessPage,
  ResendActivationPage,
  ForgotPasswordPage,
  VerifyResetOtpPage,
  ResetPasswordPage,
  PasswordResetSuccessPage,
  ChangePasswordPage,
} from "../pages/auth/AccountFlowPages";

import { getDashboardPath } from "../utils/roles";

function DashboardRedirect() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="hh-loading-screen">
        <div className="hh-loading-spinner" />
        <p>Loading HostelHub...</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Navigate to={getDashboardPath(user)} replace />;
}

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public authentication routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
<Route path="/hostels" element={<PublicHostelPage />} />
<Route path="/hostels/:id" element={<PublicHostelPage />} />
      {/* Account activation */}
      <Route path="/activate" element={<ActivateAccountPage />} />
      <Route
        path="/activation-expired"
        element={<ActivationExpiredPage />}
      />
      <Route
        path="/activation-success"
        element={<ActivationSuccessPage />}
      />
      <Route
        path="/resend-activation"
        element={<ResendActivationPage />}
      />

      {/* Password recovery */}
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/verify-reset-otp" element={<VerifyResetOtpPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route
        path="/password-reset-success"
        element={<PasswordResetSuccessPage />}
      />

      {/* Landing page redirects according to the signed-in user's role */}
      <Route path="/" element={<DashboardRedirect />} />

      {/* All routes below require authentication */}
      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<DashboardRedirect />} />

        <Route
          path="/account/change-password"
          element={<ChangePasswordPage />}
        />

        {/* Super Admin */}
        <Route element={<RoleRoute allowedRoles={["SUPER_ADMIN"]} />}>
          <Route
            path="/super-admin"
            element={<RoleDashboardPage role="SUPER_ADMIN" />}
          />
          <Route
            path="/super-admin/management"
            element={<SuperAdminManagementPage />}
          />
        </Route>

        {/* Hostel Admin */}
        <Route element={<RoleRoute allowedRoles={["HOSTEL_ADMIN"]} />}>
          <Route
            path="/hostel-admin"
            element={<RoleDashboardPage role="HOSTEL_ADMIN" />}
          />
          <Route
            path="/hostel-admin/workspace"
            element={<HostelAdminWorkspacePage />}
          />
          <Route
            path="/hostel-admin/hostels/:hostelId/staff"
            element={<HostelStaffPage />}
          />
         
 <Route
    path="/hostel-admin/hostlers"
    element={<HostlerManagementPage />}
  />
<Route
  path="/hostel-admin/complaints"
  element={<ComplaintsManagementPage />}
/>
        </Route>

        {/* Warden */}
        <Route element={<RoleRoute allowedRoles={["WARDEN"]} />}>
          <Route
            path="/warden"
            element={<RoleDashboardPage role="WARDEN" />}
          />
          <Route
  path="/warden/complaints"
  element={<ComplaintsManagementPage />}
/>
          <Route
  path="/warden/hostlers"
  element={<HostlerManagementPage />}
/>
        </Route>

        {/* Accountant */}
        <Route element={<RoleRoute allowedRoles={["ACCOUNTANT"]} />}>
          <Route
            path="/accountant"
            element={<RoleDashboardPage role="ACCOUNTANT" />}
          />
        </Route>

        {/* Staff */}
        <Route element={<RoleRoute allowedRoles={["STAFF"]} />}>
          <Route
            path="/staff"
            element={<RoleDashboardPage role="STAFF" />}
          />
        </Route>

        {/* Hostel resident */}
        <Route element={<RoleRoute allowedRoles={["HOSTLER"]} />}>
  <Route
    path="/hostler"
    element={<RoleDashboardPage role="HOSTLER" />}
  />

  <Route
    path="/hostler/dashboard"
    element={<Navigate to="/hostler" replace />}
  />

  <Route
    path="/hostler/complaints"
    element={<HostlerComplaintsPage />}
  />

  <Route
    path="/hostler/payments"
    element={<HostlerPaymentsPage />}
  />
</Route>

        {/* Standard user */}
        <Route element={<RoleRoute allowedRoles={["USER"]} />}>
          <Route
            path="/user"
            element={<RoleDashboardPage role="USER" />}
          />
        </Route>
      </Route>

      {/* Unknown routes */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

