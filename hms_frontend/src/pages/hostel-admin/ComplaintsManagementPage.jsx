import { useEffect, useMemo, useState } from "react";
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  MessageSquare,
  RefreshCw,
  Search,
  ShieldCheck,
} from "lucide-react";
import hostelAdminService from "../../services/hostelAdminService";
import complaintService from "../../services/complaintService";
import "../../styles/complaints.css";

const STATUSES = ["ALL", "PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED"];

function readable(value = "") {
  return value.replaceAll("_", " ").toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : "—";
}

export default function ComplaintsManagementPage() {
  const [hostels, setHostels] = useState([]);
  const [hostelId, setHostelId] = useState("");
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState("ALL");
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState(null);
  const [nextStatus, setNextStatus] = useState("IN_PROGRESS");
  const [adminNote, setAdminNote] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  useEffect(() => {
    let active = true;

    async function loadHostels() {
      setLoading(true);
      try {
        const data = await hostelAdminService.getMyHostels();
const list = Array.isArray(data) ? data : data?.content || [];

console.log("Assigned hostels response:", data);
console.log("First assigned hostel:", list[0]);

if (active) {
  setHostels(list);

  if (list.length > 0) {
    const firstHostel = list[0];
    const id = firstHostel.id ?? firstHostel.hostelId;

    if (id !== undefined && id !== null && String(id).trim() !== "") {
      setHostelId(String(id));
    } else {
      setError("The assigned hostel response does not contain a hostel ID.");
    }
  } else {
    setHostelId("");
  }
}
      } catch (err) {
        if (active) {
          setError(err.response?.data?.message || "Unable to load assigned hostels.");
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    loadHostels();
    return () => { active = false; };
  }, []);

  async function loadComplaints() {
    if (!hostelId) {
      setItems([]);
      return;
    }

    setLoading(true);
    setError("");
    try {
      const data = await complaintService.getHostelComplaints(hostelId, status);
      setItems(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load complaints.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadComplaints();
  }, [hostelId, status]);

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) return items;

    return items.filter((item) =>
      [
        item.subject,
        item.description,
        item.hostlerName,
        item.hostlerEmail,
        item.category,
        item.status,
      ].some((value) => String(value || "").toLowerCase().includes(query))
    );
  }, [items, search]);

  const counts = {
    pending: items.filter((x) => x.status === "PENDING").length,
    progress: items.filter((x) => x.status === "IN_PROGRESS").length,
    completed: items.filter((x) => x.status === "COMPLETED").length,
  };

  function openComplaint(item) {
    setSelected(item);
    setNextStatus(item.status);
    setAdminNote(item.adminNote || "");
    setNotice("");
    setError("");
  }

  async function saveStatus() {
    if (!selected || !hostelId) return;

    setSaving(true);
    setError("");
    setNotice("");

    try {
      await complaintService.updateComplaintStatus(
        hostelId,
        selected.id,
        { status: nextStatus, adminNote }
      );

      setSelected(null);
      setNotice("Complaint updated successfully.");
      await loadComplaints();
    } catch (err) {
      setError(err.response?.data?.message || "Unable to update complaint.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="complaints-page management-page">
      <section className="complaints-hero">
        <div>
          <span className="complaints-eyebrow">HOSTEL OPERATIONS</span>
          <h1>Complaints & Maintenance</h1>
          <p>Review resident concerns, coordinate maintenance, and keep everyone informed.</p>
        </div>
        <div className="complaints-hero-icon"><ShieldCheck size={32} /></div>
      </section>

      {error && <div className="complaint-alert error">{error}</div>}
      {notice && <div className="complaint-alert success">{notice}</div>}

      <section className="complaint-toolbar">
        <label>
          Assigned hostel
          <select value={hostelId} onChange={(e) => setHostelId(e.target.value)}>
            {hostels.map((hostel) => {
  const id = hostel.id ?? hostel.hostelId;
  const name = hostel.name ?? hostel.hostelName ?? `Hostel #${id}`;

  return (
    <option key={String(id)} value={String(id)}>
      {name}
    </option>
  );
})}
          </select>
        </label>
        <button type="button" className="complaint-secondary-button" onClick={loadComplaints}>
          <RefreshCw size={16} /> Refresh
        </button>
      </section>

      <section className="complaint-stats">
        <article>
          <span className="complaint-stat-icon"><MessageSquare /></span>
          <div><small>Total shown</small><strong>{items.length}</strong></div>
        </article>
        <article>
          <span className="complaint-stat-icon"><AlertTriangle /></span>
          <div><small>Pending</small><strong>{counts.pending}</strong></div>
        </article>
        <article>
          <span className="complaint-stat-icon"><Clock3 /></span>
          <div><small>In progress</small><strong>{counts.progress}</strong></div>
        </article>
        <article>
          <span className="complaint-stat-icon"><CheckCircle2 /></span>
          <div><small>Completed</small><strong>{counts.completed}</strong></div>
        </article>
      </section>

      <section className="complaint-panel">
        <div className="complaint-management-toolbar">
          <div className="complaint-search">
            <Search size={18} />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search subject, resident, category..."
            />
          </div>
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            {STATUSES.map((value) => (
              <option key={value} value={value}>
                {value === "ALL" ? "All statuses" : readable(value)}
              </option>
            ))}
          </select>
        </div>

        {loading ? (
          <div className="complaint-empty">Loading complaints...</div>
        ) : hostels.length === 0 ? (
          <div className="complaint-empty">
            <ShieldCheck size={30} />
            <strong>No assigned hostel found</strong>
            <p>Ask the system administrator to check your hostel assignment.</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="complaint-empty">
            <MessageSquare size={30} />
            <strong>No matching complaints</strong>
            <p>Try another status filter or search term.</p>
          </div>
        ) : (
          <div className="complaint-table-wrap">
            <table className="complaint-table">
              <thead>
                <tr>
                  <th>Complaint</th>
                  <th>Resident</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Submitted</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filtered.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <strong>{item.subject}</strong>
                      <small>{readable(item.category)}</small>
                    </td>
                    <td>
                      <strong>{item.hostlerName}</strong>
                      <small>{item.hostlerEmail}</small>
                    </td>
                    <td>
                      <span className={`complaint-priority priority-${item.priority.toLowerCase()}`}>
                        {readable(item.priority)}
                      </span>
                    </td>
                    <td>
                      <span className={`complaint-status status-${item.status.toLowerCase()}`}>
                        {readable(item.status)}
                      </span>
                    </td>
                    <td>{formatDate(item.createdAt)}</td>
                    <td>
                      <button
                        type="button"
                        className="complaint-action-button"
                        onClick={() => openComplaint(item)}
                      >
                        Review
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {selected && (
        <div className="complaint-modal-backdrop" onMouseDown={(e) => {
          if (e.target === e.currentTarget) setSelected(null);
        }}>
          <section className="complaint-modal" role="dialog" aria-modal="true">
            <div className="complaint-modal-heading">
              <div>
                <span className="complaints-eyebrow">COMPLAINT #{selected.id}</span>
                <h2>{selected.subject}</h2>
              </div>
              <button type="button" className="complaint-icon-button" onClick={() => setSelected(null)}>×</button>
            </div>

            <div className="complaint-detail-grid">
              <div><small>Resident</small><strong>{selected.hostlerName}</strong></div>
              <div><small>Category</small><strong>{readable(selected.category)}</strong></div>
              <div><small>Priority</small><strong>{readable(selected.priority)}</strong></div>
              <div><small>Submitted</small><strong>{formatDate(selected.createdAt)}</strong></div>
            </div>

            <div className="complaint-detail-description">
              <strong>Description</strong>
              <p>{selected.description}</p>
            </div>

            <label className="complaint-field">
              Update status
              <select value={nextStatus} onChange={(e) => setNextStatus(e.target.value)}>
                {STATUSES.filter((value) => value !== "ALL").map((value) => (
                  <option key={value} value={value}>{readable(value)}</option>
                ))}
              </select>
            </label>

            <label className="complaint-field">
              Response / maintenance note
              <textarea
                rows={4}
                maxLength={2000}
                value={adminNote}
                onChange={(e) => setAdminNote(e.target.value)}
                placeholder="Tell the resident what action was taken or what happens next..."
              />
            </label>

            <div className="complaint-modal-actions">
              <button type="button" className="complaint-secondary-button" onClick={() => setSelected(null)}>
                Cancel
              </button>
              <button type="button" className="complaint-primary-button" disabled={saving} onClick={saveStatus}>
                {saving ? "Saving..." : "Save update"}
              </button>
            </div>
          </section>
        </div>
      )}
    </main>
  );
}