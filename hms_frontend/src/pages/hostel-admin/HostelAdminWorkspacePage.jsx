import { Utensils } from "lucide-react";
import HostelMenuManager from "./HostelMenuManager";
import { useCallback, useEffect, useMemo, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import {
  Building2,
  Users,
  Layers3,
  Plus,
  RefreshCw,
  Search,
  MapPin,
  CheckCircle2,
  AlertCircle,
  ChevronDown,
  LayoutDashboard,
  ShieldCheck,
  X,
  Boxes,
  Pencil,
  Trash2,
} from "lucide-react";
import {  MessageSquareWarning } from "lucide-react";
import hostelAdminService from "../../services/hostelAdminService";
import hostelStaffService from "../../services/hostelStaffService";
import { getApiError } from "../../api/axiosClient";
import HostelStructureManager from "./HostelStructureManager";
import "./HostelAdminWorkspacePage.css";

const emptyBlock = { name: "", code: "", description: "" };

const emptyStaff = {
  fullName: "",
  email: "",
  phone: "",
  role: "STAFF",
};

function getMessage(error) {
  return (
    getApiError?.(error) ||
    error?.response?.data?.message ||
    error?.message ||
    "Something went wrong. Please try again."
  );
}

function StatusPill({ status }) {
  const active = String(status || "").toUpperCase() === "ACTIVE";

  return (
    <span className={`haw-status ${active ? "is-active" : "is-inactive"}`}>
      <span />
      {status || "UNKNOWN"}
    </span>
  );
}

function StatCard({ icon: Icon, label, value, tone }) {
  return (
    <motion.div
      className="haw-stat-card"
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.28 }}
    >
      <div className={`haw-stat-icon ${tone}`}>
        <Icon size={21} />
      </div>
      <div>
        <span className="haw-stat-label">{label}</span>
        <strong>{value}</strong>
      </div>
    </motion.div>
  );
}

export default function HostelAdminWorkspacePage() {
  const [assignments, setAssignments] = useState([]);
  const [selectedHostelId, setSelectedHostelId] = useState("");
  const [blocks, setBlocks] = useState([]);
  const [staff, setStaff] = useState([]);

  const [tab, setTab] = useState("overview");
  const [search, setSearch] = useState("");

  const [loadingHostels, setLoadingHostels] = useState(true);
  const [loadingWorkspace, setLoadingWorkspace] = useState(false);
  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const [showBlockForm, setShowBlockForm] = useState(false);
  const [showStaffForm, setShowStaffForm] = useState(false);
  const [editingStaff, setEditingStaff] = useState(null);

  const [blockForm, setBlockForm] = useState(emptyBlock);
  const [staffForm, setStaffForm] = useState(emptyStaff);

  const selectedHostel = useMemo(
    () =>
      assignments.find(
        (item) => String(item.hostelId) === String(selectedHostelId)
      ),
    [assignments, selectedHostelId]
  );

  const loadAssignments = useCallback(async () => {
    setLoadingHostels(true);
    setError("");

    try {
      const data = await hostelAdminService.getMyHostels();
      const list = Array.isArray(data) ? data : [];

      setAssignments(list);

      setSelectedHostelId((current) => {
        const stillAssigned = list.some(
          (item) => String(item.hostelId) === String(current)
        );

        if (current && stillAssigned) return String(current);

        return list[0]?.hostelId != null
          ? String(list[0].hostelId)
          : "";
      });
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setLoadingHostels(false);
    }
  }, []);

  const loadWorkspace = useCallback(async () => {
    if (!selectedHostelId) {
      setBlocks([]);
      setStaff([]);
      setLoadingWorkspace(false);
      return;
    }

    setLoadingWorkspace(true);
    setError("");

    try {
      const results = await Promise.allSettled([
        hostelAdminService.getBlocks(selectedHostelId),
        hostelAdminService.getStaff(selectedHostelId),
      ]);

      const errors = [];

      if (results[0].status === "fulfilled") {
        setBlocks(
          Array.isArray(results[0].value) ? results[0].value : []
        );
      } else {
        setBlocks([]);
        errors.push(
          `Blocks could not be loaded: ${getMessage(results[0].reason)}`
        );
      }

      if (results[1].status === "fulfilled") {
        setStaff(
          Array.isArray(results[1].value) ? results[1].value : []
        );
      } else {
        setStaff([]);
        errors.push(
          `Staff could not be loaded: ${getMessage(results[1].reason)}`
        );
      }

      if (errors.length) setError(errors.join(" "));
    } finally {
      setLoadingWorkspace(false);
    }
  }, [selectedHostelId]);

  useEffect(() => {
    loadAssignments();
  }, [loadAssignments]);

  useEffect(() => {
    if (!loadingHostels) loadWorkspace();
  }, [loadingHostels, loadWorkspace]);

  async function refreshAll() {
    setNotice("");
    await loadAssignments();
    await loadWorkspace();
  }

  const filteredBlocks = useMemo(() => {
    const query = search.trim().toLowerCase();

    if (!query) return blocks;

    return blocks.filter((block) =>
      [block.name, block.code, block.description, block.status]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(query))
    );
  }, [blocks, search]);

  const filteredStaff = useMemo(() => {
    const query = search.trim().toLowerCase();

    if (!query) return staff;

    return staff.filter((person) =>
      [
        person.fullName,
        person.email,
        person.phone,
        person.role,
        person.accountStatus,
      ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(query))
    );
  }, [staff, search]);

  async function submitBlock(event) {
    event.preventDefault();

    if (!selectedHostelId) {
      setError("Select a hostel before creating a block.");
      return;
    }

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await hostelAdminService.createBlock(selectedHostelId, {
        name: blockForm.name.trim(),
        code: blockForm.code.trim(),
        description: blockForm.description.trim(),
      });

      setBlockForm(emptyBlock);
      setShowBlockForm(false);
      setNotice("Block created successfully.");

      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function toggleBlockStatus(block) {
    const currentStatus = String(block.status || "").toUpperCase();
    const nextStatus = currentStatus === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    const blockId = block.id ?? block.blockId;

    if (blockId == null) {
      setError("The block ID is missing from the API response.");
      return;
    }

    const confirmed = window.confirm(
      `Change ${block.name} status to ${nextStatus}?`
    );

    if (!confirmed) return;

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await hostelAdminService.changeBlockStatus(
        selectedHostelId,
        blockId,
        nextStatus
      );

      setNotice(`${block.name} is now ${nextStatus.toLowerCase()}.`);
      await loadWorkspace();
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  function openCreateStaffForm() {
    setEditingStaff(null);
    setStaffForm(emptyStaff);
    setError("");
    setNotice("");
    setShowStaffForm(true);
  }

  function openEditStaffForm(person) {
    setEditingStaff(person);

    setStaffForm({
      fullName: person.fullName || "",
      email: person.email || "",
      phone: person.phone || "",
      role: person.role || "STAFF",
    });

    setError("");
    setNotice("");
    setShowStaffForm(true);
  }

  function closeStaffForm() {
    setShowStaffForm(false);
    setEditingStaff(null);
    setStaffForm(emptyStaff);
  }

 const submitStaff = async (event) => {
  event.preventDefault();

  if (!selectedHostelId) {
    setError("Please select a hostel first.");
    return;
  }

  setSaving(true);
  setError("");
  setNotice("");

  try {
    const payload = {
      fullName: staffForm.fullName?.trim(),
      email: staffForm.email?.trim(),
      phone: staffForm.phone?.trim() || "",
      role: staffForm.role,
    };

    if (!payload.fullName) {
      throw new Error("Full name is required.");
    }

    if (!editingStaff && !payload.email) {
      throw new Error("Email is required.");
    }

    if (!payload.role) {
      throw new Error("Please select a staff role.");
    }

    if (editingStaff) {
      const staffId =
        editingStaff.staffId ??
        editingStaff.assignmentId ??
        editingStaff.id;

      if (staffId == null) {
        throw new Error(
          "The staff ID is missing. Refresh the staff list and try again."
        );
      }

      await hostelStaffService.updateStaff(
        selectedHostelId,
        staffId,
        payload
      );

      setNotice("Team member updated successfully.");
    } else {
      await hostelStaffService.createStaff(
        selectedHostelId,
        payload
      );

      setNotice(
        "Team member created. Account activation depends on your backend workflow."
      );
    }

    closeStaffForm();

    await loadWorkspace();
  } catch (error) {
    setError(
      error?.response?.data?.message ||
      error?.message ||
      "Unable to save the team member."
    );
  } finally {
    setSaving(false);
  }
};
const removeStaff = async (person) => {
  if (!selectedHostelId) {
    setError("Please select a hostel first.");
    return;
  }

  const staffId =
    person.staffId ??
    person.assignmentId ??
    person.id;

  if (staffId == null) {
    setError(
      "The staff ID is missing. Refresh the staff list and try again."
    );
    return;
  }

  const confirmed = window.confirm(
    `Remove ${person.fullName || "this team member"} from this hostel?`
  );

  if (!confirmed) {
    return;
  }

  setSaving(true);
  setError("");
  setNotice("");

  try {
    await hostelStaffService.removeStaff(
      selectedHostelId,
      staffId
    );

    setNotice("Team member removed successfully.");

    await loadWorkspace();
  } catch (error) {
    setError(
      error?.response?.data?.message ||
      error?.message ||
      "Unable to remove the team member."
    );
  } finally {
    setSaving(false);
  }
};

    

  function changeTab(nextTab) {
    setTab(nextTab);
    setSearch("");
    setError("");
    setNotice("");
  }

  if (loadingHostels) {
    return (
      <div className="haw-loading">
        <span className="haw-spinner" />
        <p>Preparing your workspace…</p>
      </div>
    );
  }

  return (
    <main className="haw-page">
      <div className="haw-orb haw-orb-one" />
      <div className="haw-orb haw-orb-two" />

      <div className="haw-shell">
        <motion.header
          className="haw-header"
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <div className="haw-heading">
            <div className="haw-brand-mark">
              <Building2 size={25} />
            </div>

            <div>
              <p className="haw-eyebrow">HOSTEL OPERATIONS</p>
              <h1>Admin workspace</h1>
              <p className="haw-subtitle">
                Manage your assigned hostel, structure, and on-site team.
              </p>
            </div>
          </div>

          <button
            type="button"
            className="haw-button haw-button-light"
            onClick={refreshAll}
            disabled={loadingWorkspace}
          >
            <RefreshCw
              size={16}
              className={loadingWorkspace ? "haw-spin" : ""}
            />
            Refresh
          </button>
        </motion.header>

        <AnimatePresence>
          {error && (
            <motion.div
              className="haw-alert haw-alert-error"
              role="alert"
              initial={{ opacity: 0, y: -5 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
            >
              <AlertCircle size={18} />
              <span>{error}</span>
              <button
                type="button"
                aria-label="Dismiss error"
                onClick={() => setError("")}
              >
                <X size={16} />
              </button>
            </motion.div>
          )}

          {notice && (
            <motion.div
              className="haw-alert haw-alert-success"
              role="status"
              initial={{ opacity: 0, y: -5 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
            >
              <CheckCircle2 size={18} />
              <span>{notice}</span>
              <button
                type="button"
                aria-label="Dismiss message"
                onClick={() => setNotice("")}
              >
                <X size={16} />
              </button>
            </motion.div>
          )}
        </AnimatePresence>

        {assignments.length === 0 ? (
          <motion.section
            className="haw-empty-assignment"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
          >
            <div className="haw-empty-icon">
              <Building2 size={30} />
            </div>
            <h2>No hostel assigned yet</h2>
            <p>
              Your account is signed in, but no hostel assignment was returned
              by the backend. Ask your Super Admin to assign a hostel.
            </p>
            <button
              type="button"
              className="haw-button haw-button-primary"
              onClick={loadAssignments}
            >
              <RefreshCw size={16} />
              Check again
            </button>
          </motion.section>
        ) : (
          <>
            <section className="haw-hostel-banner">
              <div className="haw-hostel-copy">
                <span className="haw-label">CURRENT HOSTEL</span>

                <div className="haw-hostel-title-row">
                  <h2>{selectedHostel?.hostelName || "Assigned hostel"}</h2>
                  <ShieldCheck size={19} />
                </div>

                <p>
                  <MapPin size={15} />
                  Assigned to {selectedHostel?.adminName || "your account"}
                </p>
              </div>

              <label className="haw-hostel-picker">
                <span>Switch hostel</span>

                <span className="haw-select-wrap">
                  <select
                    value={selectedHostelId}
                    onChange={(event) => {
                      setSelectedHostelId(event.target.value);
                      setSearch("");
                      setError("");
                      setNotice("");
                    }}
                  >
                    {assignments.map((item) => (
                      <option
                        key={item.assignmentId ?? item.hostelId}
                        value={String(item.hostelId)}
                      >
                        {item.hostelName}
                      </option>
                    ))}
                  </select>
                  <ChevronDown size={16} />
                </span>
              </label>
            </section>

            <section className="haw-stats-grid">
              <StatCard
                icon={Building2}
                label="Assigned hostels"
                value={assignments.length}
                tone="purple"
              />

              <StatCard
                icon={Layers3}
                label="Blocks"
                value={blocks.length}
                tone="blue"
              />

              <StatCard
                icon={Users}
                label="Team members"
                value={staff.length}
                tone="green"
              />

              <StatCard
                icon={CheckCircle2}
                label="Active blocks"
                value={
                  blocks.filter(
                    (item) =>
                      String(item.status || "").toUpperCase() === "ACTIVE"
                  ).length
                }
                tone="orange"
              />
            </section>

            <section className="haw-workspace-card">
              <div className="haw-tabs">
                <button
                  type="button"
                  className={tab === "overview" ? "is-selected" : ""}
                  onClick={() => changeTab("overview")}
                >
                  <LayoutDashboard size={17} />
                  Overview
                </button>

                <button
                  type="button"
                  className={tab === "structure" ? "is-selected" : ""}
                  onClick={() => changeTab("structure")}
                >
                  <Boxes size={17} />
                  Structure
                </button>

                <button
                  type="button"
                  className={tab === "blocks" ? "is-selected" : ""}
                  onClick={() => changeTab("blocks")}
                >
                  <Layers3 size={17} />
                  Blocks
                  <span className="haw-tab-count">{blocks.length}</span>
                </button>

                <button
                  type="button"
                  className={tab === "staff" ? "is-selected" : ""}
                  onClick={() => changeTab("staff")}
                >
                  <Users size={17} />
                  Staff
                  <span className="haw-tab-count">{staff.length}</span>
                </button>

                
                <button
  type="button"
  className={tab === "menu" ? "is-selected" : ""}
  onClick={() => changeTab("menu")}
>
  <Utensils size={17} />
  Food Menu
</button>
<button
  type="button"
  className={tab === "complaints" ? "is-selected" : ""}
  onClick={() => {
    window.location.href = "/hostel-admin/complaints";
  }}
>
  <MessageSquareWarning size={17} />
  Complaints
</button>
              </div>
{tab === "menu" && (
  <motion.div
    className="haw-structure-panel"
    initial={{ opacity: 0, y: 6 }}
    animate={{ opacity: 1, y: 0 }}
  >
    <HostelMenuManager
      key={selectedHostelId}
      hostelId={selectedHostelId}
    />
  </motion.div>
)}
              {tab === "overview" && (
                <motion.div
                  className="haw-overview"
                  initial={{ opacity: 0, y: 5 }}
                  animate={{ opacity: 1, y: 0 }}
                >
                  <div>
                    <span className="haw-label">WORKSPACE OVERVIEW</span>
                    <h2>Manage your hostel from one place.</h2>
                    <p>
                      Create blocks, floors, optional flats, rooms, and beds.
                      Manage your on-site wardens and staff from the same
                      workspace.
                    </p>

                    <div className="haw-quick-actions">
                      <button
                        type="button"
                        className="haw-button haw-button-primary"
                        onClick={() => changeTab("structure")}
                      >
                        <Boxes size={17} />
                        Manage structure
                      </button>

                      <button
                        type="button"
                        className="haw-button haw-button-secondary"
                        onClick={() => {
                          changeTab("staff");
                          openCreateStaffForm();
                        }}
                      >
                        <Users size={17} />
                        Add team member
                      </button>
                    </div>
                  </div>

                  <div className="haw-overview-visual">
                    <div className="haw-visual-circle">
                      <Building2 size={43} />
                    </div>

                    <div className="haw-visual-chip chip-top">
                      <Layers3 size={15} />
                      Structure
                    </div>

                    <div className="haw-visual-chip chip-bottom">
                      <Users size={15} />
                      Team
                    </div>
                  </div>
                </motion.div>
              )}

              {tab === "structure" && (
                <motion.div
                  className="haw-structure-panel"
                  initial={{ opacity: 0, y: 6 }}
                  animate={{ opacity: 1, y: 0 }}
                >
                  <HostelStructureManager
                    key={selectedHostelId}
                    hostelId={selectedHostelId}
                  />
                </motion.div>
              )}

              

              {(tab === "blocks" || tab === "staff") && (
                <>
                  <div className="haw-list-toolbar">
                    <div>
                      <h2>
                        {tab === "blocks" ? "Hostel blocks" : "Team members"}
                      </h2>
                      <p>
                        {tab === "blocks"
                          ? "Organise buildings and their descriptions."
                          : "Manage staff assigned to this hostel."}
                      </p>
                    </div>

                    <button
                      type="button"
                      className="haw-button haw-button-primary"
                      onClick={() => {
                        setError("");
                        if (tab === "blocks") {
                          setShowBlockForm(true);
                        } else {
                          openCreateStaffForm();
                        }
                      }}
                    >
                      <Plus size={17} />
                      {tab === "blocks" ? "Add block" : "Add staff"}
                    </button>
                  </div>

                  <div className="haw-search">
                    <Search size={17} />

                    <input
                      value={search}
                      onChange={(event) => setSearch(event.target.value)}
                      placeholder={
                        tab === "blocks"
                          ? "Search blocks by name or code…"
                          : "Search by name, email, or role…"
                      }
                    />

                    {search && (
                      <button
                        type="button"
                        aria-label="Clear search"
                        onClick={() => setSearch("")}
                      >
                        <X size={15} />
                      </button>
                    )}
                  </div>

                  {loadingWorkspace ? (
                    <div className="haw-inline-loading">
                      <span className="haw-spinner" />
                      Loading workspace data…
                    </div>
                  ) : tab === "blocks" ? (
                    filteredBlocks.length === 0 ? (
                      <div className="haw-empty">
                        <Layers3 size={28} />
                        <h3>
                          {search ? "No matching blocks" : "No blocks yet"}
                        </h3>
                        <p>
                          {search
                            ? "Try a different search term."
                            : "Create the first block for this hostel."}
                        </p>
                      </div>
                    ) : (
                      <div className="haw-table-wrap">
                        <table className="haw-table">
                          <thead>
                            <tr>
                              <th>Block</th>
                              <th>Code</th>
                              <th>Status</th>
                              <th>Description</th>
                              <th>Actions</th>
                            </tr>
                          </thead>

                          <tbody>
                            <AnimatePresence initial={false}>
                              {filteredBlocks.map((block) => (
                                <motion.tr
                                  key={block.id ?? block.blockId}
                                  initial={{ opacity: 0 }}
                                  animate={{ opacity: 1 }}
                                  exit={{ opacity: 0 }}
                                >
                                  <td>
                                    <div className="haw-name-cell">
                                      <span className="haw-row-icon">
                                        <Building2 size={17} />
                                      </span>
                                      <strong>{block.name}</strong>
                                    </div>
                                  </td>

                                  <td>
                                    <span className="haw-code">
                                      {block.code || "—"}
                                    </span>
                                  </td>

                                  <td>
                                    <StatusPill status={block.status} />
                                  </td>

                                  <td className="haw-description">
                                    {block.description || "—"}
                                  </td>

                                  <td className="haw-actions-cell">
                                    <button
                                      type="button"
                                      className="haw-button haw-button-small"
                                      disabled={saving}
                                      onClick={() => toggleBlockStatus(block)}
                                    >
                                      {String(block.status || "").toUpperCase() ===
                                      "ACTIVE"
                                        ? "Deactivate"
                                        : "Activate"}
                                    </button>
                                  </td>
                                </motion.tr>
                              ))}
                            </AnimatePresence>
                          </tbody>
                        </table>
                      </div>
                    )
                  ) : filteredStaff.length === 0 ? (
                    <div className="haw-empty">
                      <Users size={28} />
                      <h3>
                        {search ? "No matching staff" : "No staff assigned"}
                      </h3>
                      <p>
                        {search
                          ? "Try a different search term."
                          : "Add a warden or staff member to get started."}
                      </p>
                    </div>
                  ) : (
                    <div className="haw-staff-grid">
                      {filteredStaff.map((person) => (
                        <motion.article
                          className="haw-staff-card"
                          key={
                            person.assignmentId ??
                            person.userId ??
                            person.email
                          }
                          initial={{ opacity: 0, y: 8 }}
                          animate={{ opacity: 1, y: 0 }}
                          layout
                        >
                          <div className="haw-staff-card-top">
                            <div className="haw-staff-avatar">
                              {(person.fullName || "S")
                                .trim()
                                .charAt(0)
                                .toUpperCase()}
                            </div>

                            <div className="haw-staff-actions">
                              <button
                                type="button"
                                className="haw-staff-action haw-staff-edit"
                                disabled={saving}
                                onClick={() => openEditStaffForm(person)}
                                title="Edit team member"
                              >
                                <Pencil size={15} />
                                <span>Edit</span>
                              </button>

                              <button
                                type="button"
                                className="haw-staff-action haw-staff-remove"
                                disabled={saving}
                                onClick={() => removeStaff(person)}
                                title="Remove team member"
                              >
                                <Trash2 size={15} />
                                <span>Remove</span>
                              </button>
                            </div>
                          </div>

                          <div className="haw-staff-info">
                            <h3>
                              {person.fullName || "Unnamed team member"}
                            </h3>

                            <p className="haw-staff-email">
                              {person.email || "No email provided"}
                            </p>

                            <div className="haw-staff-meta">
                              <span className="haw-staff-role">
                                {person.role || "STAFF"}
                              </span>
                              <StatusPill
                                status={person.accountStatus || "UNKNOWN"}
                              />
                            </div>

                            {person.phone && (
                              <p className="haw-phone">{person.phone}</p>
                            )}
                          </div>
                        </motion.article>
                      ))}
                    </div>
                  )}
                </>
              )}
            </section>
          </>
        )}
      </div>

      <AnimatePresence>
        {showBlockForm && (
          <div
            className="haw-modal-backdrop"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget) {
                setShowBlockForm(false);
              }
            }}
          >
            <motion.section
              className="haw-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="haw-block-title"
              initial={{ opacity: 0, y: 16, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 10, scale: 0.98 }}
            >
              <div className="haw-modal-heading">
                <div className="haw-modal-icon">
                  <Layers3 size={20} />
                </div>

                <div>
                  <h2 id="haw-block-title">Create a block</h2>
                  <p>Add a building or residential block to this hostel.</p>
                </div>

                <button
                  type="button"
                  className="haw-icon-button"
                  onClick={() => setShowBlockForm(false)}
                  aria-label="Close"
                >
                  <X size={18} />
                </button>
              </div>

              <form onSubmit={submitBlock} className="haw-form">
                <label>
                  Block name <span>*</span>
                  <input
                    required
                    maxLength={100}
                    value={blockForm.name}
                    onChange={(event) =>
                      setBlockForm({
                        ...blockForm,
                        name: event.target.value,
                      })
                    }
                    placeholder="e.g. North Block"
                  />
                </label>

                <label>
                  Block code <span>*</span>
                  <input
                    required
                    maxLength={50}
                    value={blockForm.code}
                    onChange={(event) =>
                      setBlockForm({
                        ...blockForm,
                        code: event.target.value,
                      })
                    }
                    placeholder="e.g. NB-01"
                  />
                </label>

                <label>
                  Description
                  <textarea
                    rows={3}
                    maxLength={1000}
                    value={blockForm.description}
                    onChange={(event) =>
                      setBlockForm({
                        ...blockForm,
                        description: event.target.value,
                      })
                    }
                    placeholder="Optional details about this block"
                  />
                </label>

                <div className="haw-modal-actions">
                  <button
                    type="button"
                    className="haw-button haw-button-secondary"
                    onClick={() => setShowBlockForm(false)}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    className="haw-button haw-button-primary"
                    disabled={saving}
                  >
                    {saving ? "Saving…" : "Create block"}
                  </button>
                </div>
              </form>
            </motion.section>
          </div>
        )}

        {showStaffForm && (
          <div
            className="haw-modal-backdrop"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget) closeStaffForm();
            }}
          >
            <motion.section
              className="haw-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="haw-staff-title"
              initial={{ opacity: 0, y: 16, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 10, scale: 0.98 }}
            >
              <div className="haw-modal-heading">
                <div className="haw-modal-icon">
                  <Users size={20} />
                </div>

                <div>
                  <h2 id="haw-staff-title">
                    {editingStaff ? "Edit team member" : "Add team member"}
                  </h2>
                  <p>
                    {editingStaff
                      ? "Update this team member's details."
                      : "Create a WARDEN or STAFF assignment."}
                  </p>
                </div>

                <button
                  type="button"
                  className="haw-icon-button"
                  onClick={closeStaffForm}
                  aria-label="Close"
                >
                  <X size={18} />
                </button>
              </div>

              <form onSubmit={submitStaff} className="haw-form">
                <label>
                  Full name <span>*</span>
                  <input
                    required
                    maxLength={120}
                    value={staffForm.fullName}
                    onChange={(event) =>
                      setStaffForm({
                        ...staffForm,
                        fullName: event.target.value,
                      })
                    }
                    placeholder="Enter full name"
                  />
                </label>

                <label>
                  Email address <span>*</span>
                  <input
                    required
                    type="email"
                    maxLength={254}
                    value={staffForm.email}
                    onChange={(event) =>
                      setStaffForm({
                        ...staffForm,
                        email: event.target.value,
                      })
                    }
                    placeholder="person@example.com"
                  />
                </label>

                <label>
                  Phone
                  <input
                    maxLength={30}
                    value={staffForm.phone}
                    onChange={(event) =>
                      setStaffForm({
                        ...staffForm,
                        phone: event.target.value,
                      })
                    }
                    placeholder="Optional phone number"
                  />
                </label>

                <label>
                  Role <span>*</span>
                  <select
                    value={staffForm.role}
                    onChange={(event) =>
                      setStaffForm({
                        ...staffForm,
                        role: event.target.value,
                      })
                    }
                  >
                    <option value="STAFF">Staff</option>
                    <option value="WARDEN">Warden</option>
                  </select>
                </label>

                <div className="haw-modal-actions">
                  <button
                    type="button"
                    className="haw-button haw-button-secondary"
                    onClick={closeStaffForm}
                    disabled={saving}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    className="haw-button haw-button-primary"
                    disabled={saving}
                  >
                    {saving
                      ? "Saving…"
                      : editingStaff
                        ? "Save changes"
                        : "Add team member"}
                  </button>
                </div>
              </form>
            </motion.section>
          </div>
        )}
      </AnimatePresence>
    </main>
  );
}
