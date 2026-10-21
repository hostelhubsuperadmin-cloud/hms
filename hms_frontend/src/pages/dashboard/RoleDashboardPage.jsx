
import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { Settings2 } from "lucide-react";
import {
  Activity,
  ArrowDownRight,
  ArrowRight,
  ArrowUpRight,
  Bell,
  Building2,
  CheckCircle2,
  CircleDollarSign,
  ClipboardList,
  CreditCard,
  FileText,
  LayoutDashboard,
  LogOut,
  Menu,
  RefreshCw,
  ShieldCheck,
  Users,
  X,
} from "lucide-react";

import { useAuth } from "../../auth/AuthContext";
import { dashboardService } from "../../services/dashboardService";
import "./RoleDashboardPage.css";

const ROLE_CONFIG = {
  SUPER_ADMIN: {
    title: "Super Admin",
    subtitle: "Manage your hostel network from one place.",
    accent: "violet",
  },
  HOSTEL_ADMIN: {
    title: "Hostel Admin",
    subtitle: "Your hostel operations, organized.",
    accent: "blue",
  },
  WARDEN: {
    title: "Warden",
    subtitle: "Keep residents and daily operations on track.",
    accent: "teal",
  },
  ACCOUNTANT: {
    title: "Accountant",
    subtitle: "Stay on top of billing and financial activity.",
    accent: "amber",
  },
  STAFF: {
    title: "Staff",
    subtitle: "Your daily hostel workspace.",
    accent: "rose",
  },
  HOSTLER: {
    title: "Resident Portal",
    subtitle: "Your stay, applications, and payments at a glance.",
    accent: "blue",
  },
  USER: {
    title: "My Dashboard",
    subtitle: "Welcome to your hostel management workspace.",
    accent: "violet",
  },
};

function getRoles(user) {
  const roles = Array.isArray(user?.roles) ? user.roles : [];

  return roles.map((role) =>
    String(role).replace(/^ROLE_/i, "").toUpperCase()
  );
}

function getList(response) {
  const data = response?.data;

  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.items)) return data.items;

  return [];
}

function getStatus(item) {
  return String(
    item?.status ??
    item?.applicationStatus ??
    item?.paymentStatus ??
    "UNKNOWN"
  ).replaceAll("_", " ");
}

function getDisplayName(item) {
  return (
    item?.hostelName ??
    item?.name ??
    item?.title ??
    item?.applicationNumber ??
    item?.invoiceNumber ??
    item?.referenceNumber ??
    (item?.id != null ? `Record #${item.id}` : "Record")
  );
}

function formatCurrency(value) {
  const amount = Number(value);

  if (!Number.isFinite(amount)) return "—";

  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount);
}

function getAmount(item) {
  return (
    item?.amount ??
    item?.totalAmount ??
    item?.paidAmount ??
    item?.amountPaid ??
    null
  );
}

function StatCard({ title, value, note, icon: Icon, index }) {
  return (
    <motion.article
      className="dashboard-stat"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.07, duration: 0.35 }}
    >
      <div className="stat-top">
        <span className="stat-icon">
          <Icon size={20} />
        </span>
        <span className="stat-period">Overview</span>
      </div>

      <p className="stat-title">{title}</p>
      <div className="stat-value">{value}</div>
      <p className="stat-note">{note}</p>
    </motion.article>
  );
}

function DataPanel({ title, description, items, emptyText, type }) {
  return (
    <section className="dashboard-panel">
      <div className="panel-heading">
        <div>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
        <span className="panel-count">{items.length}</span>
      </div>

      {items.length === 0 ? (
        <div className="panel-empty">
          <span className="empty-icon">
            <ClipboardList size={23} />
          </span>
          <strong>Nothing to show yet</strong>
          <p>{emptyText}</p>
        </div>
      ) : (
        <div className="activity-list">
          {items.slice(0, 5).map((item, index) => (
            <div className="activity-row" key={item.id ?? index}>
              <span className={`activity-icon ${type}`}>
                {type === "money" ? (
                  <CreditCard size={18} />
                ) : type === "hostel" ? (
                  <Building2 size={18} />
                ) : (
                  <FileText size={18} />
                )}
              </span>

              <div className="activity-main">
                <strong>{getDisplayName(item)}</strong>
                <span>
                  {item?.createdAt
                    ? new Date(item.createdAt).toLocaleDateString()
                    : getStatus(item)}
                </span>
              </div>

              <div className="activity-end">
                {getAmount(item) != null && (
                  <strong>{formatCurrency(getAmount(item))}</strong>
                )}
                <span className="status-pill">
                  {getStatus(item)}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

export default function RoleDashboardPage({ role: requestedRole }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [data, setData] = useState({
    hostels: [],
    applications: [],
    payments: [],
    invoices: [],
    notifications: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  const userRoles = getRoles(user);
  const role = requestedRole || userRoles[0] || "USER";
  const config = ROLE_CONFIG[role] || ROLE_CONFIG.USER;

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError("");

    const jobs = [];

    if (role === "SUPER_ADMIN") {
      jobs.push(
        dashboardService.getSuperAdminHostels().then((response) => {
          return ["hostels", getList(response)];
        })
      );
      jobs.push(
        dashboardService.getSuperAdminApplications().then((response) => {
          return ["applications", getList(response)];
        })
      );
    }

    if (role === "HOSTEL_ADMIN") {
      jobs.push(
        dashboardService.getHostelAdminHostels().then((response) => {
          return ["hostels", getList(response)];
        })
      );
    }

    if (role === "HOSTLER") {
      jobs.push(
        dashboardService.getMyApplications().then((response) => {
          return ["applications", getList(response)];
        })
      );
      jobs.push(
        dashboardService.getMyPayments().then((response) => {
          return ["payments", getList(response)];
        })
      );
      jobs.push(
        dashboardService.getMyInvoices().then((response) => {
          return ["invoices", getList(response)];
        })
      );
    }

    // Notifications are optional. Their failure should not hide
    // the other dashboard information.
    jobs.push(
      dashboardService.getNotifications().then((response) => {
        return ["notifications", getList(response)];
      })
    );

    const results = await Promise.allSettled(jobs);
    const nextData = {
      hostels: [],
      applications: [],
      payments: [],
      invoices: [],
      notifications: [],
    };

    let failedCount = 0;

    results.forEach((result) => {
      if (result.status === "fulfilled") {
        const [key, value] = result.value;
        nextData[key] = value;
      } else {
        failedCount += 1;
      }
    });

    setData(nextData);

    if (failedCount === jobs.length && jobs.length > 0) {
      setError(
        "Dashboard data could not be loaded. Check your session and backend API permissions."
      );
    } else if (failedCount > 0) {
      setError(
        "Some dashboard data could not be loaded. You can retry below."
      );
    }

    setLoading(false);
  }, [role]);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const stats = useMemo(() => {
    if (role === "SUPER_ADMIN") {
      return [
        {
          title: "Total hostels",
          value: data.hostels.length,
          note: "Hostels returned by the API",
          icon: Building2,
        },
        {
          title: "Applications",
          value: data.applications.length,
          note: "Applications in the directory",
          icon: ClipboardList,
        },
        {
          title: "Pending review",
          value: data.applications.filter((item) =>
            /PENDING|SUBMITTED|UNDER REVIEW/i.test(getStatus(item))
          ).length,
          note: "Awaiting a decision",
          icon: Activity,
        },
        {
          title: "Notifications",
          value: data.notifications.length,
          note: "Notifications returned by the API",
          icon: Bell,
        },
      ];
    }

    if (role === "HOSTEL_ADMIN") {
      return [
        {
          title: "Assigned hostels",
          value: data.hostels.length,
          note: "Your assigned properties",
          icon: Building2,
        },
        {
          title: "Notifications",
          value: data.notifications.length,
          note: "Updates from your workspace",
          icon: Bell,
        },
      ];
    }

    if (role === "HOSTLER") {
      return [
        {
          title: "My applications",
          value: data.applications.length,
          note: "Your submitted applications",
          icon: ClipboardList,
        },
        {
          title: "My payments",
          value: data.payments.length,
          note: "Payment records returned by the API",
          icon: CreditCard,
        },
        {
          title: "My invoices",
          value: data.invoices.length,
          note: "Your invoice records",
          icon: CircleDollarSign,
        },
        {
          title: "Notifications",
          value: data.notifications.length,
          note: "Your latest updates",
          icon: Bell,
        },
      ];
    }

    return [
      {
        title: "Notifications",
        value: data.notifications.length,
        note: "Updates for your account",
        icon: Bell,
      },
      {
        title: "Account status",
        value: "Active session",
        note: "Signed in successfully",
        icon: ShieldCheck,
      },
    ];
  }, [data, role]);

  async function handleLogout() {
    try {
      await logout();
    } finally {
      navigate("/login", { replace: true });
    }
  }

  const navItems = [
    { label: "Overview", icon: LayoutDashboard, target: "#overview" },
    { label: "Activity", icon: Activity, target: "#activity" },
    { label: "Notifications", icon: Bell, target: "#notifications" },
  ];

  return (
    <div className={`dashboard-app accent-${config.accent}`}>
      {mobileNavOpen && (
        <button
          className="dashboard-mobile-overlay"
          aria-label="Close navigation"
          onClick={() => setMobileNavOpen(false)}
        />
      )}

      <aside className={`dashboard-sidebar ${mobileNavOpen ? "is-open" : ""}`}>
        <div className="brand">
          <span className="brand-mark">
            <Building2 size={22} />
          </span>
          <span>
            <strong>HostelHub</strong>
            <small>Management portal</small>
          </span>
          <button
            className="sidebar-close"
            onClick={() => setMobileNavOpen(false)}
            aria-label="Close navigation"
          >
            <X size={19} />
          </button>
        </div>

        <div className="sidebar-section-label">WORKSPACE</div>
        <nav className="dashboard-nav">
          {navItems.map(({ label, icon: Icon, target }, index) => (
            <a
              href={target}
              className={`dashboard-nav-link ${index === 0 ? "active" : ""}`}
              key={label}
              onClick={() => setMobileNavOpen(false)}
            >
              <Icon size={18} />
              <span>{label}</span>
              {index === 0 && <span className="nav-active-dot" />}
            </a>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="security-note">
            <span><ShieldCheck size={18} /></span>
            <div>
              <strong>Secure workspace</strong>
              <small>Role-based access enabled</small>
            </div>
          </div>

          <button className="logout-button" onClick={handleLogout}>
            <LogOut size={18} />
            <span>Sign out</span>
          </button>
        </div>
      </aside>

      <main className="dashboard-main">
        <header className="dashboard-topbar">
          <button
            className="mobile-menu-button"
            onClick={() => setMobileNavOpen(true)}
            aria-label="Open navigation"
          >
            <Menu size={22} />
          </button>

          <div className="breadcrumbs">
            <span>Workspace</span>
            <span className="breadcrumb-slash">/</span>
            <strong>Overview</strong>
          </div>

          <div className="topbar-user">
            <button
              className="topbar-icon-button"
              aria-label="Go to notifications"
              onClick={() =>
                document.getElementById("notifications")?.scrollIntoView({
                  behavior: "smooth",
                })
              }
            >
              <Bell size={19} />
              {data.notifications.length > 0 && (
                <span className="notification-dot" />
              )}
            </button>

            <div className="user-avatar">
              {(user?.fullName || user?.name || user?.email || "U")
                .trim()
                .charAt(0)
                .toUpperCase()}
            </div>
            <div className="user-info">
              <strong>{user?.fullName || user?.name || "Welcome"}</strong>
              <span>{config.title}</span>
            </div>
          </div>
        </header>

        <div className="dashboard-content" id="overview">
          <motion.section
            className="dashboard-welcome"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
          >
            <div className="welcome-copy">
              <span className="welcome-eyebrow">
                <span className="welcome-live-dot" />
                YOUR WORKSPACE
              </span>
              <h1>
                Welcome back{user?.fullName ? "," : ""}
                <br />
                <span>{user?.fullName?.split(" ")[0] || config.title}</span>
              </h1>
              <p>{config.subtitle}</p>
            </div>

            <div className="welcome-art" aria-hidden="true">
              <div className="art-orbit orbit-one" />
              <div className="art-orbit orbit-two" />
              <div className="art-building">
                <Building2 size={66} strokeWidth={1.25} />
              </div>
              <span className="art-spark spark-one">✦</span>
              <span className="art-spark spark-two">✧</span>
            </div>
          </motion.section>

          {error && (
            <div className="dashboard-alert" role="alert">
              <span>{error}</span>
              <button onClick={loadDashboard}>
                <RefreshCw size={15} /> Retry
              </button>
            </div>
          )}

          <div className="section-title-row">
            <div>
              <span className="section-eyebrow">AT A GLANCE</span>
              <h2>Your overview</h2>
            </div>
            <button
              className="refresh-button"
              onClick={loadDashboard}
              disabled={loading}
            >
              <RefreshCw size={15} className={loading ? "spin" : ""} />
              Refresh data
            </button>
          </div>
{role === "SUPER_ADMIN" && (
  <Link
    to="/super-admin/management"
    className="dashboard-management-link"
  >
    <Settings2 size={18} />
    <span>Platform management</span>
    <ArrowRight size={16} />
  </Link>
)}
{role === "HOSTEL_ADMIN" && (
  <Link
    to="/hostel-admin/workspace"
    className="dashboard-management-link"
  >
    <Building2 size={18} />
    <span>Open hostel operations</span>
    <ArrowRight size={16} />
  </Link>
)}
{role === "HOSTEL_ADMIN" && (
  <Link
    to="/hostel-admin/hostlers"
    className="dashboard-management-link"
  >
    <Users size={18} />
    <span>Hostlers, allocations & billing</span>
    <ArrowRight size={16} />
  </Link>
)}

{role === "WARDEN" && (
  <Link
    to="/warden/hostlers"
    className="dashboard-management-link"
  >
    <Users size={18} />
    <span>Hostlers, allocations & billing</span>
    <ArrowRight size={16} />
  </Link>
)}
{role === "HOSTLER" && (
  <>
    <Link
      to="/hostler/complaints"
      className="dashboard-management-link"
    >
      <ClipboardList size={18} />
      <span>Complaints & Maintenance</span>
      <ArrowRight size={16} />
    </Link>

    <Link
      to="/hostler/payments"
      className="dashboard-management-link"
    >
      <CreditCard size={18} />
      <span>My invoices & payments</span>
      <ArrowRight size={16} />
    </Link>
  </>
)}

{role === "USER" && (
  <Link
    to="/hostels"
    className="dashboard-management-link"
  >
    <Building2 size={18} />
    <span>Explore Hostels</span>
    <ArrowRight size={16} />
  </Link>
)}


          <section className="dashboard-stats">
            {stats.map((stat, index) => (
              <StatCard {...stat} key={stat.title} index={index} />
            ))}
          </section>

          {loading && (
            <div className="dashboard-loading" role="status">
              <span className="loading-spinner" />
              Loading your latest information…
            </div>
          )}

          <div className="dashboard-panels" id="activity">
            {(role === "SUPER_ADMIN" || role === "HOSTEL_ADMIN") && (
              <DataPanel
                title={role === "SUPER_ADMIN" ? "Hostel directory" : "Your hostels"}
                description="Hostel records available to your account"
                items={data.hostels}
                emptyText="No hostel records were returned by the backend."
                type="hostel"
              />
            )}

            {(role === "SUPER_ADMIN" || role === "HOSTLER") && (
              <DataPanel
                title={role === "SUPER_ADMIN" ? "Recent applications" : "My applications"}
                description="Application records from your account"
                items={data.applications}
                emptyText="Application records will appear here when available."
                type="application"
              />
            )}

            {role === "HOSTLER" && (
              <>
                <DataPanel
                  title="Payment history"
                  description="Your recent payment records"
                  items={data.payments}
                  emptyText="No payment records were returned."
                  type="money"
                />
                <DataPanel
                  title="Invoices"
                  description="Your recent billing records"
                  items={data.invoices}
                  emptyText="No invoices were returned."
                  type="money"
                />
              </>
            )}

            {role !== "SUPER_ADMIN" &&
              role !== "HOSTEL_ADMIN" &&
              role !== "HOSTLER" && (
                <section className="dashboard-panel quick-panel">
                  <div className="panel-heading">
                    <div>
                      <h2>Your workspace is ready</h2>
                      <p>Tools and records will appear here as modules are connected.</p>
                    </div>
                    <CheckCircle2 size={23} />
                  </div>
                  <div className="quick-panel-body">
                    <div className="quick-panel-icon">
                      <ShieldCheck size={24} />
                    </div>
                    <div>
                      <strong>{config.title} access verified</strong>
                      <p>
                        You are signed in. The next phase will connect the
                        role-specific operational modules supported by your backend.
                      </p>
                    </div>
                  </div>
                </section>
              )}

            <section className="dashboard-panel notifications-panel" id="notifications">
              <div className="panel-heading">
                <div>
                  <h2>Recent notifications</h2>
                  <p>Updates returned by your notification service</p>
                </div>
                <Bell size={21} />
              </div>

              {data.notifications.length === 0 ? (
                <div className="notification-empty">
                  <span className="empty-icon"><Bell size={22} /></span>
                  <strong>You're all caught up</strong>
                  <p>No notifications are currently available.</p>
                </div>
              ) : (
                <div className="activity-list">
                  {data.notifications.slice(0, 5).map((item, index) => (
                    <div className="activity-row" key={item.id ?? index}>
                      <span className="activity-icon notification">
                        <Bell size={17} />
                      </span>
                      <div className="activity-main">
                        <strong>
                          {item.title || item.subject || "Notification"}
                        </strong>
                        <span>
                          {item.message || item.body || getStatus(item)}
                        </span>
                      </div>
                      <span className="status-pill">
                        {item.read ? "READ" : "NEW"}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </div>

          <footer className="dashboard-footer">
            <span>HostelHub · Management portal</span>
            <span><ShieldCheck size={14} /> Secure session</span>
          </footer>
        </div>
      </main>
    </div>
  );
}
