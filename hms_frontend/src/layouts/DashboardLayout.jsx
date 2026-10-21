
import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import {
  Bell,
  Building2,
  ChartNoAxesCombined,
  ChevronRight,
  LayoutDashboard,
  LogOut,
  Menu,
  Settings,
  ShieldCheck,
  Users,
  X,
  MessageSquareWarning
} from "lucide-react";
import "./DashboardLayout.css";
import { useAuth } from "../auth/AuthContext";
import { getDashboardPath, normalizeRoles, ROLES } from "../utils/roles";

const menuByRole = {
  [ROLES.SUPER_ADMIN]: [
    { label: "Overview", path: "/super-admin/dashboard", icon: LayoutDashboard },
    { label: "Hostels", path: "/super-admin/hostels", icon: Building2 },
    { label: "Users", path: "/super-admin/users", icon: Users },
  ],
  [ROLES.HOSTEL_ADMIN]: [
    { label: "Overview", path: "/hostel-admin/dashboard", icon: LayoutDashboard },
    { label: "Rooms & Beds", path: "/hostel-admin/rooms", icon: Building2 },
    { label: "Staff", path: "/hostel-admin/staff", icon: Users },
    {
  label: "Manage Complaints",
  path: "/hostel-admin/complaints",
  icon: Bell,
},
  ],
  [ROLES.WARDEN]: [
  { label: "Overview", path: "/warden/dashboard", icon: LayoutDashboard },
  { label: "Residents", path: "/warden/residents", icon: Users },
  {
    label: "Manage Complaints",
    path: "/warden/complaints",
    icon: Bell,
  },
],
  [ROLES.ACCOUNTANT]: [
    { label: "Overview", path: "/accountant/dashboard", icon: ChartNoAxesCombined },
    { label: "Invoices", path: "/accountant/invoices", icon: Building2 },
    { label: "Payments", path: "/accountant/payments", icon: Bell },
  ],
  [ROLES.STAFF]: [
    { label: "Overview", path: "/staff/dashboard", icon: LayoutDashboard },
  ],
  [ROLES.HOSTLER]: [
  {
    label: "My Dashboard",
    path: "/hostler/dashboard",
    icon: LayoutDashboard,
  },
  {
    label: "My Profile",
    path: "/hostler/profile",
    icon: Users,
  },
  {
    label: "Complaints & Maintenance",
    path: "/hostler/complaints",
    icon: MessageSquareWarning,
  },
],
  [ROLES.USER]: [
    { label: "My Dashboard", path: "/user/dashboard", icon: LayoutDashboard },
    { label: "My Profile", path: "/user/profile", icon: Users },
  ],
};

export default function DashboardLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const roles = normalizeRoles(user);
  const role = roles.find((item) => menuByRole[item]) || ROLES.USER;
  const menu = menuByRole[role] || [];
  const displayName =
    user?.fullName || user?.name || user?.email || "Account";

  async function handleLogout() {
    try {
      await logout();
    } finally {
      navigate("/login", { replace: true });
    }
  }

  function SidebarContent() {
    return (
      <>
        <div className="sidebar-brand">
          <div className="brand-mark">
            <Building2 size={22} />
          </div>
          <div>
            <strong>HostelHub</strong>
            <span>Management Suite</span>
          </div>
        </div>

        <div className="sidebar-label">WORKSPACE</div>

        <nav className="sidebar-nav">
          {menu.map(({ label, path, icon: Icon }) => (
            <NavLink
              key={path}
              to={path}
              end
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active" : ""}`
              }
            >
              <Icon size={19} />
              <span>{label}</span>
              <ChevronRight size={15} className="nav-chevron" />
            </NavLink>
          ))}

          <NavLink
            to={`${getDashboardPath(user).replace("/dashboard", "")}/settings`}
            className={({ isActive }) =>
              `sidebar-link ${isActive ? "active" : ""}`
            }
            onClick={() => setMobileOpen(false)}
          >
            <Settings size={19} />
            <span>Settings</span>
          </NavLink>
        </nav>

        <div className="sidebar-bottom">
          <div className="security-note">
            <ShieldCheck size={20} />
            <div>
              <strong>Secure workspace</strong>
              <span>Role-protected access</span>
            </div>
          </div>

          <button
            className="logout-button"
            onClick={handleLogout}
            type="button"
          >
            <LogOut size={18} />
            Sign out
          </button>
        </div>
      </>
    );
  }

  return (
    <div className="dashboard-shell">
      <aside className="desktop-sidebar">
        <SidebarContent />
      </aside>

      <AnimatePresence>
        {mobileOpen && (
          <>
            <motion.button
              type="button"
              aria-label="Close navigation"
              className="sidebar-backdrop"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setMobileOpen(false)}
            />

            <motion.aside
              className="mobile-sidebar"
              initial={{ x: -280 }}
              animate={{ x: 0 }}
              exit={{ x: -280 }}
              transition={{ duration: 0.22 }}
            >
              <button
                className="mobile-close"
                onClick={() => setMobileOpen(false)}
                aria-label="Close menu"
                type="button"
              >
                <X size={21} />
              </button>
              <SidebarContent />
            </motion.aside>
          </>
        )}
      </AnimatePresence>

      <div className="dashboard-main">
        <header className="topbar">
          <button
            className="mobile-menu-button"
            type="button"
            aria-label="Open navigation"
            onClick={() => setMobileOpen(true)}
          >
            <Menu size={22} />
          </button>

          <div className="topbar-heading">
            <span>HOSTELHUB / WORKSPACE</span>
            <strong>
              {role.replaceAll("_", " ")}
            </strong>
          </div>

          <div className="topbar-profile">
            <div className="profile-avatar">
              {displayName.charAt(0).toUpperCase()}
            </div>
            <div className="profile-copy">
              <strong>{displayName}</strong>
              <span>{role.replaceAll("_", " ")}</span>
            </div>
          </div>
        </header>

        <main className="dashboard-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
