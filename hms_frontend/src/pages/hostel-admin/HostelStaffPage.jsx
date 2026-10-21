
import { useCallback, useEffect, useState } from "react";
import {
  ArrowLeft,
  Users,
  UserPlus,
  Pencil,
  Trash2,
  X,
  Mail,
  Phone,
  ShieldCheck,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Clock,
} from "lucide-react";

import hostelStaffService from "../../services/hostelStaffService";
import "./hostel-staff.css";

const getStaffId = (staff) =>
  staff?.staffId ?? staff?.id ?? staff?.userId ?? staff?.staff?.id;

const getStaffName = (staff) =>
  staff?.name ??
  staff?.fullName ??
  staff?.staffName ??
  staff?.user?.name ??
  "Unnamed staff";

const getStaffEmail = (staff) =>
  staff?.email ?? staff?.user?.email ?? "";

const getStaffPhone = (staff) =>
  staff?.phoneNumber ??
  staff?.phone ??
  staff?.mobileNumber ??
  "";

const getStaffRole = (staff) =>
  String(staff?.role ?? staff?.user?.role ?? "STAFF")
    .replace(/^ROLE_/, "")
    .toUpperCase();

const getStaffStatus = (staff) =>
  String(staff?.status ?? staff?.accountStatus ?? "UNKNOWN")
    .replaceAll(" ", "_")
    .toUpperCase();

const getInitial = (name) =>
  String(name || "S").trim().charAt(0).toUpperCase() || "S";

const getErrorMessage = (error) =>
  error?.response?.data?.message ||
  error?.response?.data?.error ||
  error?.message ||
  "Something went wrong. Please try again.";

const unwrapStaffList = (response) => {
  const data = response?.data ?? response;

  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.staff)) return data.staff;
  if (Array.isArray(data?.members)) return data.members;
  if (Array.isArray(data?.data)) return data.data;

  return [];
};

const emptyForm = {
  name: "",
  email: "",
  phoneNumber: "",
  role: "STAFF",
};

export default function HostelStaffPage({
  hostelId,
  onBack,
}) {
  const [staffMembers, setStaffMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [pageError, setPageError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const [modalOpen, setModalOpen] = useState(false);
  const [editingStaff, setEditingStaff] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [formError, setFormError] = useState("");

  const loadStaff = useCallback(async () => {
    if (!hostelId) {
      setLoading(false);
      setPageError("Hostel ID is missing. Please select a hostel first.");
      return;
    }

    setLoading(true);
    setPageError("");

    try {
      const response = await hostelStaffService.getStaff(hostelId);
      setStaffMembers(unwrapStaffList(response));
    } catch (error) {
      setPageError(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [hostelId]);

  useEffect(() => {
    loadStaff();
  }, [loadStaff]);

  const openAddModal = () => {
    setEditingStaff(null);
    setForm(emptyForm);
    setFormError("");
    setSuccessMessage("");
    setModalOpen(true);
  };

  const openEditModal = (staff) => {
    setEditingStaff(staff);
    setForm({
      name: getStaffName(staff) === "Unnamed staff"
        ? ""
        : getStaffName(staff),
      email: getStaffEmail(staff),
      phoneNumber: getStaffPhone(staff),
      role: getStaffRole(staff),
    });
    setFormError("");
    setSuccessMessage("");
    setModalOpen(true);
  };

  const closeModal = () => {
    if (saving) return;

    setModalOpen(false);
    setEditingStaff(null);
    setForm(emptyForm);
    setFormError("");
  };

  const handleInputChange = (event) => {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFormError("");
    setSuccessMessage("");

    if (!hostelId) {
      setFormError("Hostel ID is missing.");
      return;
    }

    const name = form.name.trim();
    const email = form.email.trim();
    const phoneNumber = form.phoneNumber.trim();

    if (!name || !email) {
      setFormError("Please enter the staff member's name and email.");
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setFormError("Please enter a valid email address.");
      return;
    }

    const payload = {
      name,
      email,
      phoneNumber,
      role: form.role,
    };

    setSaving(true);

    try {
      if (editingStaff) {
        const staffId = getStaffId(editingStaff);

        if (staffId == null) {
          throw new Error(
            "This staff record has no ID, so it cannot be updated."
          );
        }

        await hostelStaffService.updateStaff(
          hostelId,
          staffId,
          payload
        );

        setSuccessMessage("Staff member updated successfully.");
      } else {
        await hostelStaffService.addStaff(hostelId, payload);
        setSuccessMessage("Staff member added successfully.");
      }

      setModalOpen(false);
      setEditingStaff(null);
      setForm(emptyForm);
      await loadStaff();
    } catch (error) {
      setFormError(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveStaff = async (staff) => {
    const staffId = getStaffId(staff);

    if (staffId == null) {
      setPageError(
        "This staff record has no ID. Check the staff API response and ID field."
      );
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to remove ${getStaffName(staff)} from this hostel?`
    );

    if (!confirmed) return;

    setPageError("");
    setSuccessMessage("");

    try {
      await hostelStaffService.removeStaff(hostelId, staffId);

      setStaffMembers((current) =>
        current.filter((member) => getStaffId(member) !== staffId)
      );

      setSuccessMessage("Staff member removed successfully.");
    } catch (error) {
      setPageError(getErrorMessage(error));
    }
  };

  const getStatusClass = (status) => {
    if (status === "ACTIVE") return "staff-status-active";
    if (status.includes("PENDING")) return "staff-status-pending";
    if (status === "INACTIVE" || status === "DISABLED") {
      return "staff-status-inactive";
    }

    return "staff-status-default";
  };

  const getStatusIcon = (status) => {
    if (status === "ACTIVE") return <CheckCircle2 size={14} />;
    if (status.includes("PENDING")) return <Clock size={14} />;
    return <AlertCircle size={14} />;
  };

  return (
    <main className="staff-page">
      <header className="staff-page-header">
        <div className="staff-heading-left">
          {onBack && (
            <button
              type="button"
              className="staff-back-button"
              onClick={onBack}
              aria-label="Go back"
            >
              <ArrowLeft size={19} />
            </button>
          )}

          <div className="staff-heading-icon">
            <Users size={23} />
          </div>

          <div>
            <h1>Team members</h1>
            <p>Manage staff assigned to this hostel.</p>
          </div>
        </div>

        <div className="staff-header-actions">
          <button
            type="button"
            className="staff-secondary-button"
            onClick={loadStaff}
            disabled={loading}
          >
            <RefreshCw
              size={16}
              className={loading ? "staff-spin" : ""}
            />
            Refresh
          </button>

          <button
            type="button"
            className="staff-primary-button"
            onClick={openAddModal}
          >
            <UserPlus size={17} />
            Add staff
          </button>
        </div>
      </header>

      {pageError && (
        <div className="staff-alert staff-alert-error" role="alert">
          <AlertCircle size={18} />
          <span>{pageError}</span>
          <button
            type="button"
            onClick={() => setPageError("")}
            aria-label="Dismiss error"
          >
            <X size={16} />
          </button>
        </div>
      )}

      {successMessage && (
        <div className="staff-alert staff-alert-success" role="status">
          <CheckCircle2 size={18} />
          <span>{successMessage}</span>
          <button
            type="button"
            onClick={() => setSuccessMessage("")}
            aria-label="Dismiss message"
          >
            <X size={16} />
          </button>
        </div>
      )}

      <section className="staff-list-section">
        <div className="staff-list-heading">
          <div>
            <h2>Staff directory</h2>
            <p>
              {staffMembers.length}{" "}
              {staffMembers.length === 1 ? "member" : "members"}
            </p>
          </div>
        </div>

        {loading ? (
          <div className="staff-empty-state">
            <RefreshCw size={28} className="staff-spin" />
            <p>Loading team members...</p>
          </div>
        ) : staffMembers.length === 0 ? (
          <div className="staff-empty-state">
            <Users size={35} />
            <h3>No team members yet</h3>
            <p>Add staff members to manage your hostel team.</p>
            <button
              type="button"
              className="staff-primary-button"
              onClick={openAddModal}
            >
              <UserPlus size={17} />
              Add staff
            </button>
          </div>
        ) : (
          <div className="staff-grid">
            {staffMembers.map((staff, index) => {
              const name = getStaffName(staff);
              const email = getStaffEmail(staff);
              const phone = getStaffPhone(staff);
              const role = getStaffRole(staff);
              const status = getStaffStatus(staff);
              const staffId = getStaffId(staff);

              return (
                <article
                  className="staff-card"
                  key={staffId ?? `${email}-${index}`}
                >
                  <div className="staff-card-top">
                    <div className="staff-avatar">
                      {getInitial(name)}
                    </div>

                    <div className="staff-member-identity">
                      <h3>{name}</h3>
                      <p className="staff-email">
                        <Mail size={14} />
                        <span>{email || "No email provided"}</span>
                      </p>
                    </div>
                  </div>

                  <div className="staff-member-meta">
                    <span className="staff-role-badge">
                      <ShieldCheck size={14} />
                      {role}
                    </span>

                    <span
                      className={`staff-status-badge ${getStatusClass(status)}`}
                    >
                      {getStatusIcon(status)}
                      {status.replaceAll("_", " ")}
                    </span>
                  </div>

                  <div className="staff-phone">
                    <Phone size={15} />
                    <span>{phone || "No phone number"}</span>
                  </div>

                  {/* Actions are always rendered for each staff member. */}
                  <div className="staff-row-actions">
                    <button
                      type="button"
                      className="staff-action-button"
                      onClick={() => openEditModal(staff)}
                    >
                      <Pencil size={15} />
                      Edit
                    </button>

                    <button
                      type="button"
                      className="staff-action-button staff-action-danger"
                      onClick={() => handleRemoveStaff(staff)}
                    >
                      <Trash2 size={15} />
                      Remove
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>

      {modalOpen && (
        <div
          className="staff-modal-overlay"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) closeModal();
          }}
        >
          <section
            className="staff-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="staff-modal-title"
          >
            <div className="staff-modal-header">
              <div>
                <h2 id="staff-modal-title">
                  {editingStaff ? "Edit staff member" : "Add staff member"}
                </h2>
                <p>
                  {editingStaff
                    ? "Update the staff member's details."
                    : "Enter the details of the staff member."}
                </p>
              </div>

              <button
                type="button"
                className="staff-modal-close"
                onClick={closeModal}
                disabled={saving}
                aria-label="Close dialog"
              >
                <X size={20} />
              </button>
            </div>

            <form className="staff-form" onSubmit={handleSubmit}>
              {formError && (
                <div className="staff-alert staff-alert-error" role="alert">
                  <AlertCircle size={17} />
                  <span>{formError}</span>
                </div>
              )}

              <label htmlFor="staff-name">Full name</label>
              <input
                id="staff-name"
                name="name"
                type="text"
                value={form.name}
                onChange={handleInputChange}
                placeholder="Enter full name"
                autoComplete="name"
                required
              />

              <label htmlFor="staff-email">Email address</label>
              <input
                id="staff-email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleInputChange}
                placeholder="name@example.com"
                autoComplete="email"
                required
              />

              <label htmlFor="staff-phone">Phone number</label>
              <input
                id="staff-phone"
                name="phoneNumber"
                type="tel"
                value={form.phoneNumber}
                onChange={handleInputChange}
                placeholder="Enter phone number"
                autoComplete="tel"
              />

              <label htmlFor="staff-role">Role</label>
              <select
                id="staff-role"
                name="role"
                value={form.role}
                onChange={handleInputChange}
                required
              >
                <option value="STAFF">Staff</option>
                <option value="WARDEN">Warden</option>
              </select>

              <div className="staff-form-actions">
                <button
                  type="button"
                  className="staff-secondary-button"
                  onClick={closeModal}
                  disabled={saving}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="staff-primary-button"
                  disabled={saving}
                >
                  {saving
                    ? "Saving..."
                    : editingStaff
                      ? "Save changes"
                      : "Add staff"}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </main>
  );
}
