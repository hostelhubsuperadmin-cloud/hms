
import { useCallback, useEffect, useMemo, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import {
  Activity,
  ArrowDownUp,
  Building2,
  Check,
  CheckCircle2,
  ChevronRight,
  CircleAlert,
  ClipboardCheck,
  Eye,
  FileText,
  LoaderCircle,
  Mail,
  MapPin,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  ShieldCheck,
  ToggleLeft,
  ToggleRight,
  Users,
  X,Trash2,Unlink
} from "lucide-react";

import { useAuth } from "../../auth/AuthContext";
import superAdminService from "../../services/superAdminService";
import { getApiError } from "../../api/axiosClient";
import "./SuperAdminManagementPage.css";

const EMPTY_HOSTEL = {
  name: "",
  code: "",
  description: "",
  addressLine: "",
  city: "",
  state: "",
  country: "India",
  postalCode: "",
  contactPhone: "",
  contactEmail: "",
};

const EMPTY_ADMIN = {
  fullName: "",
  email: "",
  phone: "",
};

const TABS = [
  { id: "hostels", label: "Hostels", icon: Building2 },
  { id: "admins", label: "Hostel admins", icon: Users },
  { id: "applications", label: "Applications", icon: ClipboardCheck },
];

function extractArray(response) {
  const data = response?.data;

  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.items)) return data.items;

  return [];
}

function formatDate(value) {
  if (!value) return "—";

  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? "—"
    : date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      });
}

function displayStatus(value) {
  return String(value || "UNKNOWN")
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function StatusBadge({ status }) {
  const normalized = String(status || "unknown").toLowerCase();

  return (
    <span className={`sa-status sa-status--${normalized}`}>
      <span className="sa-status-dot" />
      {displayStatus(status)}
    </span>
  );
}

function StatCard({ icon: Icon, label, value, tone, detail }) {
  return (
    <motion.article
      className="sa-stat-card"
      whileHover={{ y: -4 }}
      transition={{ duration: 0.18 }}
    >
      <div className={`sa-stat-icon sa-stat-icon--${tone}`}>
        <Icon size={21} />
      </div>

      <div className="sa-stat-copy">
        <span>{label}</span>
        <strong>{value}</strong>
        <small>{detail}</small>
      </div>
    </motion.article>
  );
}

function Modal({ title, subtitle, onClose, children, wide = false }) {
  useEffect(() => {
    function onKeyDown(event) {
      if (event.key === "Escape") onClose();
    }

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  return (
    <motion.div
      className="sa-modal-backdrop"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <motion.section
        className={`sa-modal ${wide ? "sa-modal--wide" : ""}`}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        initial={{ opacity: 0, y: 18, scale: 0.98 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        exit={{ opacity: 0, y: 12, scale: 0.98 }}
        transition={{ duration: 0.2 }}
      >
        <header className="sa-modal-header">
          <div>
            <h2>{title}</h2>
            {subtitle && <p>{subtitle}</p>}
          </div>

          <button
            className="sa-icon-button"
            type="button"
            onClick={onClose}
            aria-label="Close dialog"
          >
            <X size={19} />
          </button>
        </header>

        <div className="sa-modal-body">{children}</div>
      </motion.section>
    </motion.div>
  );
}

function Field({
  label,
  name,
  value,
  onChange,
  required = false,
  type = "text",
  placeholder = "",
  multiline = false,
  maxLength,
}) {
  const common = {
    id: name,
    name,
    value: value ?? "",
    onChange,
    required,
    placeholder,
    maxLength,
  };

  return (
    <label className="sa-field" htmlFor={name}>
      <span>
        {label} {required && <i>*</i>}
      </span>

      {multiline ? (
        <textarea {...common} rows={3} />
      ) : (
        <input {...common} type={type} />
      )}
    </label>
  );
}

function EmptyState({ icon: Icon = FileText, title, description }) {
  return (
    <div className="sa-empty-state">
      <div className="sa-empty-icon">
        <Icon size={25} />
      </div>
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

export default function SuperAdminManagementPage() {
  const { user } = useAuth();

  const [activeTab, setActiveTab] = useState("hostels");
  const [hostels, setHostels] = useState([]);
  const [admins, setAdmins] = useState([]);
  const [applications, setApplications] = useState([]);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const [search, setSearch] = useState("");
  const [applicationFilter, setApplicationFilter] = useState("ALL");

  const [hostelModal, setHostelModal] = useState(false);
  const [editingHostel, setEditingHostel] = useState(null);
  const [hostelForm, setHostelForm] = useState(EMPTY_HOSTEL);

  const [adminModal, setAdminModal] = useState(false);
  const [adminForm, setAdminForm] = useState(EMPTY_ADMIN);

  const [assignModal, setAssignModal] = useState(false);
  const [selectedAdminId, setSelectedAdminId] = useState("");
  const [selectedHostelId, setSelectedHostelId] = useState("");

  const [reviewModal, setReviewModal] = useState(false);
  const [selectedApplication, setSelectedApplication] = useState(null);
  const [reviewDecision, setReviewDecision] = useState("APPROVED");
  const [reviewNotes, setReviewNotes] = useState("");
const [unassignModal, setUnassignModal] = useState(false);
const [unassignAdmin, setUnassignAdmin] = useState(null);
const [adminAssignments, setAdminAssignments] = useState([]);
const [selectedUnassignHostelId, setSelectedUnassignHostelId] = useState("");
  const loadData = useCallback(async () => {
    setLoading(true);
    setError("");

    const results = await Promise.allSettled([
      superAdminService.getHostels(),
      superAdminService.getHostelAdmins(),
      superAdminService.getApplications(),
    ]);

    const failures = [];

    if (results[0].status === "fulfilled") {
      setHostels(extractArray(results[0].value));
    } else {
      failures.push(`Hostels: ${getApiError(results[0].reason)}`);
    }

    if (results[1].status === "fulfilled") {
      setAdmins(extractArray(results[1].value));
    } else {
      failures.push(`Hostel admins: ${getApiError(results[1].reason)}`);
    }

    if (results[2].status === "fulfilled") {
      setApplications(extractArray(results[2].value));
    } else {
      failures.push(`Applications: ${getApiError(results[2].reason)}`);
    }

    if (failures.length) setError(failures.join(" "));
    setLoading(false);
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const filteredHostels = useMemo(() => {
    const query = search.trim().toLowerCase();

    return hostels.filter((hostel) => {
      const searchable = [
        hostel.name,
        hostel.code,
        hostel.city,
        hostel.state,
        hostel.status,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return searchable.includes(query);
    });
  }, [hostels, search]);

  const filteredAdmins = useMemo(() => {
    const query = search.trim().toLowerCase();

    return admins.filter((admin) =>
      [admin.fullName, admin.email, admin.phone, admin.status]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(query)
    );
  }, [admins, search]);

  const filteredApplications = useMemo(() => {
    const query = search.trim().toLowerCase();

    return applications.filter((application) => {
      const statusMatches =
        applicationFilter === "ALL" ||
        application.status === applicationFilter;

      const searchable = [
        application.applicantName,
        application.applicantEmail,
        application.hostelName,
        application.status,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return statusMatches && searchable.includes(query);
    });
  }, [applications, search, applicationFilter]);

  const activeHostelCount = hostels.filter((item) =>
    String(item.status).toUpperCase() === "ACTIVE"
  ).length;

  const pendingApplications = applications.filter(
    (item) => item.status === "PENDING"
  ).length;

  function openCreateHostel() {
    setEditingHostel(null);
    setHostelForm(EMPTY_HOSTEL);
    setError("");
    setHostelModal(true);
  }

  function openEditHostel(hostel) {
    setEditingHostel(hostel);

    setHostelForm({
      name: hostel.name || "",
      code: hostel.code || "",
      description: hostel.description || "",
      addressLine: hostel.addressLine || "",
      city: hostel.city || "",
      state: hostel.state || "",
      country: hostel.country || "India",
      postalCode: hostel.postalCode || "",
      contactPhone: hostel.contactPhone || "",
      contactEmail: hostel.contactEmail || "",
    });

    setError("");
    setHostelModal(true);
  }

  function updateHostelField(event) {
    const { name, value } = event.target;

    setHostelForm((current) => ({
      ...current,
      [name]: value,
    }));
  }

  async function submitHostel(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setNotice("");

    try {
      if (editingHostel) {
        await superAdminService.updateHostel(
          editingHostel.id,
          hostelForm
        );
        setNotice("Hostel details updated successfully.");
      } else {
        await superAdminService.createHostel(hostelForm);
        setNotice("Hostel created successfully.");
      }

      setHostelModal(false);
      await loadData();
    } catch (requestError) {
      setError(getApiError(requestError));
    } finally {
      setSaving(false);
    }
  }

  async function toggleHostelStatus(hostel) {
    const currentStatus = String(hostel.status || "").toUpperCase();
    const nextStatus = currentStatus === "ACTIVE" ? "INACTIVE" : "ACTIVE";

    const confirmed = window.confirm(
      `${nextStatus === "ACTIVE" ? "Activate" : "Deactivate"} "${hostel.name}"?`
    );

    if (!confirmed) return;

    setError("");
    setNotice("");

    try {
      await superAdminService.updateHostelStatus(hostel.id, nextStatus);
      setNotice(`Hostel status changed to ${nextStatus}.`);
      await loadData();
    } catch (requestError) {
      setError(getApiError(requestError));
    }
  }

  async function submitAdmin(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setNotice("");

    try {
      await superAdminService.createHostelAdmin(adminForm);
      setAdminModal(false);
      setAdminForm(EMPTY_ADMIN);
      setNotice(
        "Hostel Admin created. Check your backend invitation/activation flow for the next step."
      );
      await loadData();
    } catch (requestError) {
      setError(getApiError(requestError));
    } finally {
      setSaving(false);
    }
  }
async function openUnassignModal(admin) {
  setError("");
  setNotice("");
  setUnassignAdmin(admin);
  setAdminAssignments([]);
  setSelectedUnassignHostelId("");
  setUnassignModal(true);
  setSaving(true);

  try {
    const response = await superAdminService.getAdminAssignments(admin.id);
    setAdminAssignments(extractArray(response));
  } catch (requestError) {
    setError(getApiError(requestError));
  } finally {
    setSaving(false);
  }
}

async function submitUnassignment(event) {
  event.preventDefault();

  if (!unassignAdmin || !selectedUnassignHostelId) {
    setError("Select a hostel to unassign.");
    return;
  }

  const selectedHostel = adminAssignments.find(
    (assignment) =>
      String(assignment.hostelId) === String(selectedUnassignHostelId)
  );

  if (!selectedHostel) {
    setError("The selected hostel assignment could not be found.");
    return;
  }

  const confirmed = window.confirm(
    `Unassign ${unassignAdmin.fullName} from ` +
    `${selectedHostel.hostelName}?\n\n` +
    "The admin account and hostel will remain unchanged."
  );

  if (!confirmed) return;

  setSaving(true);
  setError("");
  setNotice("");

  try {
    await superAdminService.unassignAdminFromHostel(
      unassignAdmin.id,
      selectedUnassignHostelId
    );

    setUnassignModal(false);
    setUnassignAdmin(null);
    setAdminAssignments([]);
    setSelectedUnassignHostelId("");

    setNotice(
      `${unassignAdmin.fullName} was unassigned from ` +
      `${selectedHostel.hostelName}.`
    );

    await loadData();
  } catch (requestError) {
    setError(getApiError(requestError));
  } finally {
    setSaving(false);
  }
}
  async function submitAssignment(event) {
    event.preventDefault();

    if (!selectedAdminId || !selectedHostelId) {
      setError("Select both an admin and a hostel.");
      return;
    }

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await superAdminService.assignAdmin(
        selectedAdminId,
        selectedHostelId
      );

      setAssignModal(false);
      setSelectedAdminId("");
      setSelectedHostelId("");
      setNotice("Hostel Admin assignment completed.");
      await loadData();
    } catch (requestError) {
      setError(getApiError(requestError));
    } finally {
      setSaving(false);
    }
  }
  async function deleteHostelAdmin(admin) {
  const confirmed = window.confirm(
    `PERMANENTLY DELETE HOSTEL ADMIN ACCOUNT?\n\n` +
    `Name: ${admin.fullName}\n` +
    `Email: ${admin.email}\n\n` +
    "The account cannot be recovered. Hostel assignments will be removed. " +
    "Deletion will be rejected if other records still depend on this account.\n\n" +
    "Do you want to continue?"
  );

  if (!confirmed) return;

  setError("");
  setNotice("");

  try {
    await superAdminService.deleteHostelAdmin(admin.id);

    setNotice(
      `Hostel Admin account "${admin.fullName}" was permanently deleted.`
    );

    await loadData();
  } catch (requestError) {
    setError(getApiError(requestError));
  }
}

  function openReview(application, decision) {
    setSelectedApplication(application);
    setReviewDecision(decision);
    setReviewNotes("");
    setError("");
    setReviewModal(true);
  }

  async function submitReview(event) {
    event.preventDefault();

    if (!selectedApplication) return;

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await superAdminService.reviewApplication(
        selectedApplication.id,
        {
          status: reviewDecision,
          reviewNotes: reviewNotes.trim() || null,
        }
      );

      setReviewModal(false);
      setSelectedApplication(null);
      setNotice(`Application ${reviewDecision.toLowerCase()} successfully.`);
      await loadData();
    } catch (requestError) {
      setError(getApiError(requestError));
    } finally {
      setSaving(false);
    }
  }

  const greetingName =
    user?.fullName || user?.name || user?.email || "Administrator";

  return (
    <main className="sa-page">
      <div className="sa-orb sa-orb--one" />
      <div className="sa-orb sa-orb--two" />

      <div className="sa-content">
        <motion.header
          className="sa-page-header"
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <div>
            <div className="sa-eyebrow">
              <ShieldCheck size={15} />
              <span>HOSTELHUB CONTROL CENTER</span>
            </div>

            <h1>Management <span>workspace</span></h1>

            <p>
              Welcome back, {greetingName}. Manage hostels, administrators,
              and applications from one place.
            </p>
          </div>

          <button
            className="sa-button sa-button--secondary"
            type="button"
            onClick={loadData}
            disabled={loading}
          >
            <RefreshCw size={16} className={loading ? "sa-spin" : ""} />
            Refresh data
          </button>
        </motion.header>

        <section className="sa-stat-grid">
          <StatCard
            icon={Building2}
            label="Registered hostels"
            value={hostels.length}
            tone="violet"
            detail={`${activeHostelCount} currently active`}
          />

          <StatCard
            icon={Users}
            label="Hostel administrators"
            value={admins.length}
            tone="blue"
            detail="Across the platform"
          />

          <StatCard
            icon={ClipboardCheck}
            label="Pending applications"
            value={pendingApplications}
            tone="amber"
            detail="Awaiting your review"
          />

          <StatCard
            icon={Activity}
            label="Total applications"
            value={applications.length}
            tone="green"
            detail="All recorded statuses"
          />
        </section>

        {error && (
          <div className="sa-alert sa-alert--error" role="alert">
            <CircleAlert size={18} />
            <span>{error}</span>
            <button
              type="button"
              className="sa-alert-close"
              onClick={() => setError("")}
              aria-label="Dismiss error"
            >
              <X size={16} />
            </button>
          </div>
        )}

        {notice && (
          <div className="sa-alert sa-alert--success" role="status">
            <CheckCircle2 size={18} />
            <span>{notice}</span>
            <button
              type="button"
              className="sa-alert-close"
              onClick={() => setNotice("")}
              aria-label="Dismiss notification"
            >
              <X size={16} />
            </button>
          </div>
        )}

        <section className="sa-workspace">
          <div className="sa-workspace-top">
            <div>
              <span className="sa-section-kicker">OPERATIONS</span>
              <h2>Manage your platform</h2>
            </div>

            <div className="sa-workspace-actions">
              {activeTab === "hostels" && (
                <button
                  className="sa-button sa-button--primary"
                  onClick={openCreateHostel}
                  type="button"
                >
                  <Plus size={17} />
                  Add hostel
                </button>
              )}

              {activeTab === "admins" && (
                <>
                  <button
                    className="sa-button sa-button--secondary"
                    onClick={() => setAssignModal(true)}
                    type="button"
                  >
                    <ArrowDownUp size={16} />
                    Assign admin
                  </button>

                  <button
                    className="sa-button sa-button--primary"
                    onClick={() => {
                      setAdminForm(EMPTY_ADMIN);
                      setAdminModal(true);
                    }}
                    type="button"
                  >
                    <Plus size={17} />
                    Add admin
                  </button>
                </>
              )}
            </div>
          </div>

          <div className="sa-tab-bar" role="tablist" aria-label="Management sections">
            {TABS.map(({ id, label, icon: Icon }) => (
              <button
                key={id}
                type="button"
                role="tab"
                aria-selected={activeTab === id}
                className={`sa-tab ${activeTab === id ? "sa-tab--active" : ""}`}
                onClick={() => {
                  setActiveTab(id);
                  setSearch("");
                }}
              >
                <Icon size={17} />
                {label}

                {id === "applications" && pendingApplications > 0 && (
                  <span className="sa-tab-count">{pendingApplications}</span>
                )}
              </button>
            ))}
          </div>

          <div className="sa-table-toolbar">
            <label className="sa-search">
              <Search size={17} />
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder={
                  activeTab === "hostels"
                    ? "Search by hostel, code or location..."
                    : activeTab === "admins"
                      ? "Search by name, email or phone..."
                      : "Search by applicant or hostel..."
                }
                aria-label="Search records"
              />

              {search && (
                <button
                  type="button"
                  onClick={() => setSearch("")}
                  aria-label="Clear search"
                >
                  <X size={15} />
                </button>
              )}
            </label>

            {activeTab === "applications" && (
              <select
                className="sa-filter-select"
                value={applicationFilter}
                onChange={(event) => setApplicationFilter(event.target.value)}
                aria-label="Filter applications by status"
              >
                <option value="ALL">All statuses</option>
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            )}

            <span className="sa-result-count">
              {activeTab === "hostels"
                ? filteredHostels.length
                : activeTab === "admins"
                  ? filteredAdmins.length
                  : filteredApplications.length}{" "}
              records
            </span>
          </div>

          {loading ? (
            <div className="sa-loading">
              <LoaderCircle size={30} className="sa-spin" />
              <p>Loading management data...</p>
            </div>
          ) : (
            <AnimatePresence mode="wait">
              <motion.div
                key={activeTab}
                initial={{ opacity: 0, y: 7 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -5 }}
                transition={{ duration: 0.16 }}
                className="sa-table-wrap"
              >
                {activeTab === "hostels" && (
                  filteredHostels.length ? (
                    <table className="sa-table">
                      <thead>
                        <tr>
                          <th>Hostel</th>
                          <th>Location</th>
                          <th>Contact</th>
                          <th>Status</th>
                          <th>Created</th>
                          <th>Actions</th>
                        </tr>
                      </thead>

                      <tbody>
                        {filteredHostels.map((hostel) => (
                          <tr key={hostel.id}>
                            <td>
                              <div className="sa-primary-cell">
                                <div className="sa-row-avatar sa-row-avatar--violet">
                                  <Building2 size={18} />
                                </div>
                                <div>
                                  <strong>{hostel.name}</strong>
                                  <small>{hostel.code}</small>
                                </div>
                              </div>
                            </td>

                            <td>
                              <div className="sa-location-cell">
                                <MapPin size={15} />
                                <span>
                                  {[hostel.city, hostel.state]
                                    .filter(Boolean)
                                    .join(", ") || "—"}
                                </span>
                              </div>
                            </td>

                            <td>
                              <div className="sa-contact-cell">
                                <span>{hostel.contactEmail || "No email"}</span>
                                <small>{hostel.contactPhone || "No phone"}</small>
                              </div>
                            </td>

                            <td><StatusBadge status={hostel.status} /></td>
                            <td>{formatDate(hostel.createdAt)}</td>

                            <td>
                              <div className="sa-row-actions">
                                <button
                                  type="button"
                                  className="sa-icon-button"
                                  title="Edit hostel"
                                  aria-label={`Edit ${hostel.name}`}
                                  onClick={() => openEditHostel(hostel)}
                                >
                                  <Pencil size={16} />
                                </button>

                                <button
                                  type="button"
                                  className="sa-icon-button"
                                  title={
                                    String(hostel.status).toUpperCase() === "ACTIVE"
                                      ? "Deactivate hostel"
                                      : "Activate hostel"
                                  }
                                  aria-label={`Change status for ${hostel.name}`}
                                  onClick={() => toggleHostelStatus(hostel)}
                                >
                                  {String(hostel.status).toUpperCase() === "ACTIVE"
                                    ? <ToggleRight size={19} />
                                    : <ToggleLeft size={19} />}
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <EmptyState
                      icon={Building2}
                      title="No hostels found"
                      description="Create your first hostel or change your search."
                    />
                  )
                )}

                {activeTab === "admins" && (
                  filteredAdmins.length ? (
                    <table className="sa-table">
                      <thead>
                        <tr>
                          <th>Administrator</th>
                          <th>Contact</th>
                          <th>Account status</th>
                          <th>Email verified</th>
                          <th>Actions</th>
                        </tr>
                      </thead>

                      <tbody>
                        {filteredAdmins.map((admin) => (
                          <tr key={admin.id}>
                            <td>
                              <div className="sa-primary-cell">
                                <div className="sa-row-avatar sa-row-avatar--blue">
                                  <Users size={18} />
                                </div>
                                <div>
                                  <strong>{admin.fullName || "Unnamed admin"}</strong>
                                  <small>ID #{admin.id}</small>
                                </div>
                              </div>
                            </td>

                            <td>
                              <div className="sa-contact-cell">
                                <span>{admin.email}</span>
                                <small>{admin.phone || "No phone provided"}</small>
                              </div>
                            </td>

                            <td><StatusBadge status={admin.status} /></td>

                            <td>
                              <span className={`sa-verified ${admin.emailVerified ? "is-verified" : ""}`}>
                                {admin.emailVerified
                                  ? <CheckCircle2 size={15} />
                                  : <CircleAlert size={15} />}
                                {admin.emailVerified ? "Verified" : "Not verified"}
                              </span>
                            </td>

                            <td>
  <div className="sa-row-actions">
   <td>
  <div className="sa-row-actions">
    <button
      className="sa-button sa-button--small sa-button--secondary"
      type="button"
      onClick={() => {
        setSelectedAdminId(String(admin.id));
        setSelectedHostelId("");
        setAssignModal(true);
      }}
    >
      Assign hostel
      <ChevronRight size={15} />
    </button>

    <button
      className="sa-icon-button"
      type="button"
      title="Unassign from a hostel"
      aria-label={`Unassign ${admin.fullName} from a hostel`}
      onClick={() => openUnassignModal(admin)}
    >
      <Unlink size={16} />
    </button>
  </div>
</td>
    <button
      type="button"
      className="sa-icon-button sa-icon-button--danger"
      title="Permanently delete Hostel Admin account"
      aria-label={`Delete Hostel Admin account for ${admin.fullName}`}
      onClick={() => deleteHostelAdmin(admin)}
    >
      <Trash2 size={16} />
    </button>
  </div>
</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <EmptyState
                      icon={Users}
                      title="No administrators found"
                      description="Create a Hostel Admin before assigning one to a hostel."
                    />
                  )
                )}
{unassignModal && unassignAdmin && (
  <Modal
    title="Unassign hostel"
    subtitle={`Choose which hostel ${unassignAdmin.fullName} should no longer manage.`}
    onClose={() => {
      if (saving) return;
      setUnassignModal(false);
      setUnassignAdmin(null);
      setAdminAssignments([]);
      setSelectedUnassignHostelId("");
    }}
  >
    <form className="sa-form" onSubmit={submitUnassignment}>
      <div className="sa-info-note">
        <ShieldCheck size={17} />
        <span>
          This removes only the selected assignment. The admin account,
          role, hostel and hostel data remain unchanged.
        </span>
      </div>

      <label className="sa-field" htmlFor="unassign-hostel">
        <span>Assigned hostel <i>*</i></span>

        <select
          id="unassign-hostel"
          value={selectedUnassignHostelId}
          onChange={(event) =>
            setSelectedUnassignHostelId(event.target.value)
          }
          disabled={saving}
          required
        >
          <option value="">Select an assigned hostel</option>

          {adminAssignments.map((assignment) => (
            <option
              key={assignment.id}
              value={String(assignment.hostelId)}
            >
              {assignment.hostelName} (ID #{assignment.hostelId})
            </option>
          ))}
        </select>
      </label>

      {!saving && adminAssignments.length === 0 && (
        <p className="sa-muted">
          This admin is not currently assigned to any hostel.
        </p>
      )}

      <div className="sa-modal-actions">
        <button
          type="button"
          className="sa-button sa-button--secondary"
          onClick={() => {
            setUnassignModal(false);
            setUnassignAdmin(null);
            setAdminAssignments([]);
            setSelectedUnassignHostelId("");
          }}
          disabled={saving}
        >
          Cancel
        </button>

        <button
          type="submit"
          className="sa-button sa-button--primary"
          disabled={
            saving ||
            !selectedUnassignHostelId ||
            adminAssignments.length === 0
          }
        >
          {saving
            ? <LoaderCircle className="sa-spin" size={17} />
            : <Unlink size={17} />}
          Unassign hostel
        </button>
      </div>
    </form>
  </Modal>
)}
                {activeTab === "applications" && (
                  filteredApplications.length ? (
                    <table className="sa-table">
                      <thead>
                        <tr>
                          <th>Applicant</th>
                          <th>Requested hostel</th>
                          <th>Move-in date</th>
                          <th>Submitted</th>
                          <th>Status</th>
                          <th>Review</th>
                        </tr>
                      </thead>

                      <tbody>
                        {filteredApplications.map((application) => (
                          <tr key={application.id}>
                            <td>
                              <div className="sa-primary-cell">
                                <div className="sa-row-avatar sa-row-avatar--amber">
                                  <FileText size={18} />
                                </div>
                                <div>
                                  <strong>{application.applicantName || "Applicant"}</strong>
                                  <small>{application.applicantEmail}</small>
                                </div>
                              </div>
                            </td>

                            <td>{application.hostelName || "—"}</td>
                            <td>{formatDate(application.requestedMoveInDate)}</td>
                            <td>{formatDate(application.createdAt)}</td>
                            <td><StatusBadge status={application.status} /></td>

                            <td>
                              {application.status === "PENDING" ? (
                                <div className="sa-row-actions">
                                  <button
                                    type="button"
                                    className="sa-icon-button sa-icon-button--approve"
                                    title="Approve application"
                                    aria-label={`Approve ${application.applicantName}`}
                                    onClick={() => openReview(application, "APPROVED")}
                                  >
                                    <Check size={17} />
                                  </button>

                                  <button
                                    type="button"
                                    className="sa-icon-button sa-icon-button--reject"
                                    title="Reject application"
                                    aria-label={`Reject ${application.applicantName}`}
                                    onClick={() => openReview(application, "REJECTED")}
                                  >
                                    <X size={17} />
                                  </button>
                                </div>
                              ) : (
                                <span className="sa-reviewed-label">
                                  Reviewed
                                </span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <EmptyState
                      icon={ClipboardCheck}
                      title="No applications found"
                      description="Applications will appear here when users submit them."
                    />
                  )
                )}
              </motion.div>
            </AnimatePresence>
          )}
        </section>

        <footer className="sa-footer">
          <span><ShieldCheck size={15} /> Role-protected management workspace</span>
          <span>HostelHub <i>•</i> Administration</span>
        </footer>
      </div>

      <AnimatePresence>
        {hostelModal && (
          <Modal
            title={editingHostel ? "Edit hostel" : "Create a hostel"}
            subtitle="Enter the hostel details below. Required fields are marked."
            onClose={() => setHostelModal(false)}
            wide
          >
            <form className="sa-form" onSubmit={submitHostel}>
              <div className="sa-form-grid">
                <Field
                  label="Hostel name"
                  name="name"
                  value={hostelForm.name}
                  onChange={updateHostelField}
                  required
                  maxLength={150}
                  placeholder="e.g. Sunrise Residency"
                />

                <Field
                  label="Hostel code"
                  name="code"
                  value={hostelForm.code}
                  onChange={updateHostelField}
                  required
                  maxLength={50}
                  placeholder="e.g. SR-VJA-01"
                />

                <Field
                  label="Address line"
                  name="addressLine"
                  value={hostelForm.addressLine}
                  onChange={updateHostelField}
                  required
                  maxLength={255}
                  placeholder="Street, area, building"
                />

                <Field
                  label="City"
                  name="city"
                  value={hostelForm.city}
                  onChange={updateHostelField}
                  required
                  maxLength={100}
                />

                <Field
                  label="State"
                  name="state"
                  value={hostelForm.state}
                  onChange={updateHostelField}
                  required
                  maxLength={100}
                />

                <Field
                  label="Country"
                  name="country"
                  value={hostelForm.country}
                  onChange={updateHostelField}
                  required
                  maxLength={100}
                />

                <Field
                  label="Postal code"
                  name="postalCode"
                  value={hostelForm.postalCode}
                  onChange={updateHostelField}
                  maxLength={20}
                />

                <Field
                  label="Contact phone"
                  name="contactPhone"
                  value={hostelForm.contactPhone}
                  onChange={updateHostelField}
                  maxLength={20}
                  placeholder="+91..."
                />

                <Field
                  label="Contact email"
                  name="contactEmail"
                  value={hostelForm.contactEmail}
                  onChange={updateHostelField}
                  type="email"
                  maxLength={254}
                />

                <Field
                  label="Description"
                  name="description"
                  value={hostelForm.description}
                  onChange={updateHostelField}
                  multiline
                  maxLength={2000}
                  placeholder="A short description of the hostel..."
                />
              </div>

              <div className="sa-modal-actions">
                <button
                  type="button"
                  className="sa-button sa-button--secondary"
                  onClick={() => setHostelModal(false)}
                  disabled={saving}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="sa-button sa-button--primary"
                  disabled={saving}
                >
                  {saving ? <LoaderCircle className="sa-spin" size={17} /> : <Check size={17} />}
                  {editingHostel ? "Save changes" : "Create hostel"}
                </button>
              </div>
            </form>
          </Modal>
        )}

        {adminModal && (
          <Modal
            title="Create Hostel Admin"
            subtitle="Create an administrator account using the backend's supported fields."
            onClose={() => setAdminModal(false)}
          >
            <form className="sa-form" onSubmit={submitAdmin}>
              <Field
                label="Full name"
                name="fullName"
                value={adminForm.fullName}
                onChange={(event) => setAdminForm((form) => ({
                  ...form,
                  fullName: event.target.value,
                }))}
                required
                maxLength={120}
                placeholder="Administrator's full name"
              />

              <Field
                label="Email address"
                name="email"
                value={adminForm.email}
                onChange={(event) => setAdminForm((form) => ({
                  ...form,
                  email: event.target.value,
                }))}
                required
                type="email"
                maxLength={255}
                placeholder="admin@example.com"
              />

              <Field
                label="Phone number"
                name="phone"
                value={adminForm.phone}
                onChange={(event) => setAdminForm((form) => ({
                  ...form,
                  phone: event.target.value,
                }))}
                maxLength={30}
                placeholder="Optional phone number"
              />

              <div className="sa-info-note">
                <Mail size={17} />
                <span>
                  Account activation or invitation behavior is controlled by
                  your backend. This form does not invent a password.
                </span>
              </div>

              <div className="sa-modal-actions">
                <button
                  type="button"
                  className="sa-button sa-button--secondary"
                  onClick={() => setAdminModal(false)}
                  disabled={saving}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="sa-button sa-button--primary"
                  disabled={saving}
                >
                  {saving ? <LoaderCircle className="sa-spin" size={17} /> : <Plus size={17} />}
                  Create admin
                </button>
              </div>
            </form>
          </Modal>
        )}

        {assignModal && (
          <Modal
            title="Assign administrator"
            subtitle="Choose an existing Hostel Admin and the hostel they should manage."
            onClose={() => setAssignModal(false)}
          >
            <form className="sa-form" onSubmit={submitAssignment}>
              <label className="sa-field" htmlFor="assign-admin">
                <span>Hostel Admin <i>*</i></span>
                <select
                  id="assign-admin"
                  value={selectedAdminId}
                  onChange={(event) => setSelectedAdminId(event.target.value)}
                  required
                >
                  <option value="">Select an administrator</option>
                  {admins.map((admin) => (
                    <option key={admin.id} value={admin.id}>
                      {admin.fullName} — {admin.email}
                    </option>
                  ))}
                </select>
              </label>

              <label className="sa-field" htmlFor="assign-hostel">
                <span>Hostel <i>*</i></span>
                <select
                  id="assign-hostel"
                  value={selectedHostelId}
                  onChange={(event) => setSelectedHostelId(event.target.value)}
                  required
                >
                  <option value="">Select a hostel</option>
                  {hostels.map((hostel) => (
                    <option key={hostel.id} value={hostel.id}>
                      {hostel.name} ({hostel.code})
                    </option>
                  ))}
                </select>
              </label>

              <div className="sa-info-note">
                <ShieldCheck size={17} />
                <span>
                  Assignment permissions are enforced by Spring Security on
                  the backend.
                </span>
              </div>

              <div className="sa-modal-actions">
                <button
                  type="button"
                  className="sa-button sa-button--secondary"
                  onClick={() => setAssignModal(false)}
                  disabled={saving}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="sa-button sa-button--primary"
                  disabled={saving || !admins.length || !hostels.length}
                >
                  {saving ? <LoaderCircle className="sa-spin" size={17} /> : <Check size={17} />}
                  Confirm assignment
                </button>
              </div>
            </form>
          </Modal>
        )}

        {reviewModal && selectedApplication && (
          <Modal
            title={`${reviewDecision === "APPROVED" ? "Approve" : "Reject"} application`}
            subtitle="Review the applicant details before submitting your decision."
            onClose={() => setReviewModal(false)}
            wide
          >
            <div className="sa-applicant-summary">
              <div className="sa-row-avatar sa-row-avatar--amber">
                <FileText size={21} />
              </div>

              <div>
                <h3>{selectedApplication.applicantName}</h3>
                <p>{selectedApplication.applicantEmail}</p>
              </div>

              <StatusBadge status={selectedApplication.status} />
            </div>

            <div className="sa-detail-grid">
              <div><span>Hostel</span><strong>{selectedApplication.hostelName || "—"}</strong></div>
              <div><span>Move-in date</span><strong>{formatDate(selectedApplication.requestedMoveInDate)}</strong></div>
              <div><span>Date of birth</span><strong>{formatDate(selectedApplication.dateOfBirth)}</strong></div>
              <div><span>Gender</span><strong>{selectedApplication.gender || "—"}</strong></div>
              <div><span>Guardian</span><strong>{selectedApplication.guardianName || "—"}</strong></div>
              <div><span>Guardian phone</span><strong>{selectedApplication.guardianPhone || "—"}</strong></div>
              <div><span>Emergency contact</span><strong>{selectedApplication.emergencyContact || "—"}</strong></div>
              <div><span>Address</span><strong>{[
                selectedApplication.addressLine,
                selectedApplication.city,
                selectedApplication.state,
                selectedApplication.postalCode,
              ].filter(Boolean).join(", ") || "—"}</strong></div>
            </div>

            {selectedApplication.applicantNotes && (
              <div className="sa-applicant-notes">
                <span>Applicant notes</span>
                <p>{selectedApplication.applicantNotes}</p>
              </div>
            )}

            <form className="sa-form" onSubmit={submitReview}>
              <label className="sa-field" htmlFor="review-decision">
                <span>Decision <i>*</i></span>
                <select
                  id="review-decision"
                  value={reviewDecision}
                  onChange={(event) => setReviewDecision(event.target.value)}
                  required
                >
                  <option value="APPROVED">Approve application</option>
                  <option value="REJECTED">Reject application</option>
                </select>
              </label>

              <Field
                label="Review notes"
                name="reviewNotes"
                value={reviewNotes}
                onChange={(event) => setReviewNotes(event.target.value)}
                multiline
                maxLength={2000}
                placeholder="Optional notes for the applicant..."
              />

              <div className="sa-modal-actions">
                <button
                  type="button"
                  className="sa-button sa-button--secondary"
                  onClick={() => setReviewModal(false)}
                  disabled={saving}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className={`sa-button ${reviewDecision === "APPROVED" ? "sa-button--primary" : "sa-button--danger"}`}
                  disabled={saving}
                >
                  {saving ? <LoaderCircle className="sa-spin" size={17} /> : <Eye size={17} />}
                  Submit decision
                </button>
              </div>
            </form>
          </Modal>
        )}
      </AnimatePresence>
    </main>
  );
}
