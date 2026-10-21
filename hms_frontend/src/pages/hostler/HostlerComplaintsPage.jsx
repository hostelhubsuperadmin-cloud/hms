import { useEffect, useState } from "react";
import {
  AlertCircle,
  CheckCircle2,
  Clock3,
  FilePlus2,
  MessageSquare,
  RefreshCw,
  Send,
} from "lucide-react";
import complaintService from "../../services/complaintService";
import "../../styles/complaints.css";

const CATEGORIES = [
  "FOOD",
  "ROOM",
  "WATER",
  "ELECTRICITY",
  "CLEANLINESS",
  "PLUMBING",
  "OTHER",
];

const PRIORITIES = ["LOW", "MEDIUM", "HIGH", "URGENT"];

function readable(value = "") {
  return value.replaceAll("_", " ").toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function dateText(value) {
  return value ? new Date(value).toLocaleString() : "—";
}

export default function HostlerComplaintsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [form, setForm] = useState({
    subject: "",
    category: "FOOD",
    priority: "MEDIUM",
    description: "",
  });

  async function loadComplaints() {
    setLoading(true);
    setError("");
    try {
      const data = await complaintService.getMyComplaints();
      setItems(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to load your complaints."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadComplaints();
  }, []);

  function changeField(event) {
    setForm((previous) => ({
      ...previous,
      [event.target.name]: event.target.value,
    }));
  }

  async function submitComplaint(event) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    setNotice("");

    try {
      await complaintService.createComplaint(form);
      setForm({
        subject: "",
        category: "FOOD",
        priority: "MEDIUM",
        description: "",
      });
      setNotice("Your complaint was submitted successfully.");
      await loadComplaints();
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to submit your complaint."
      );
    } finally {
      setSubmitting(false);
    }
  }

  const openCount = items.filter(
    (item) => !["COMPLETED", "REJECTED"].includes(item.status)
  ).length;

  return (
    <main className="complaints-page">
      <section className="complaints-hero">
        <div>
          <span className="complaints-eyebrow">RESIDENT SUPPORT</span>
          <h1>Complaints & Maintenance</h1>
          <p>
            Tell us what needs attention. Track every request until it is
            resolved.
          </p>
        </div>
        <div className="complaints-hero-icon">
          <MessageSquare size={32} />
        </div>
      </section>

      <section className="complaint-stats">
        <article>
          <span className="complaint-stat-icon"><FilePlus2 /></span>
          <div><small>Total complaints</small><strong>{items.length}</strong></div>
        </article>
        <article>
          <span className="complaint-stat-icon"><Clock3 /></span>
          <div><small>Awaiting resolution</small><strong>{openCount}</strong></div>
        </article>
        <article>
          <span className="complaint-stat-icon"><CheckCircle2 /></span>
          <div>
            <small>Resolved</small>
            <strong>{items.filter((x) => x.status === "COMPLETED").length}</strong>
          </div>
        </article>
      </section>

      {error && <div className="complaint-alert error">{error}</div>}
      {notice && <div className="complaint-alert success">{notice}</div>}

      <div className="complaint-layout">
        <section className="complaint-panel">
          <div className="complaint-panel-heading">
            <div>
              <h2>Raise a complaint</h2>
              <p>Describe the issue so your hostel team can help.</p>
            </div>
            <FilePlus2 size={23} />
          </div>

          <form className="complaint-form" onSubmit={submitComplaint}>
            <label>
              Subject
              <input
                name="subject"
                value={form.subject}
                onChange={changeField}
                maxLength={150}
                placeholder="e.g. Water supply unavailable"
                required
              />
            </label>

            <div className="complaint-form-grid">
              <label>
                Category
                <select name="category" value={form.category} onChange={changeField}>
                  {CATEGORIES.map((value) => (
                    <option key={value} value={value}>{readable(value)}</option>
                  ))}
                </select>
              </label>

              <label>
                Priority
                <select name="priority" value={form.priority} onChange={changeField}>
                  {PRIORITIES.map((value) => (
                    <option key={value} value={value}>{readable(value)}</option>
                  ))}
                </select>
              </label>
            </div>

            <label>
              Describe the issue
              <textarea
                name="description"
                value={form.description}
                onChange={changeField}
                maxLength={3000}
                rows={5}
                placeholder="Include the location and details of the issue..."
                required
              />
              <small>{form.description.length}/3000 characters</small>
            </label>

            <button className="complaint-primary-button" disabled={submitting}>
              <Send size={17} />
              {submitting ? "Submitting..." : "Submit complaint"}
            </button>
          </form>
        </section>

        <section className="complaint-panel complaint-history">
          <div className="complaint-panel-heading">
            <div>
              <h2>My complaints</h2>
              <p>Review the latest updates from your hostel team.</p>
            </div>
            <button
              type="button"
              className="complaint-icon-button"
              onClick={loadComplaints}
              title="Refresh complaints"
            >
              <RefreshCw size={17} />
            </button>
          </div>

          {loading ? (
            <div className="complaint-empty">Loading your complaints...</div>
          ) : items.length === 0 ? (
            <div className="complaint-empty">
              <AlertCircle size={30} />
              <strong>No complaints yet</strong>
              <p>Use the form to report an issue whenever you need help.</p>
            </div>
          ) : (
            <div className="complaint-list">
              {items.map((item) => (
                <article className="complaint-card" key={item.id}>
                  <div className="complaint-card-top">
                    <span className={`complaint-status status-${item.status.toLowerCase()}`}>
                      {readable(item.status)}
                    </span>
                    <span className={`complaint-priority priority-${item.priority.toLowerCase()}`}>
                      {readable(item.priority)}
                    </span>
                  </div>
                  <h3>{item.subject}</h3>
                  <p className="complaint-description">{item.description}</p>
                  <div className="complaint-card-meta">
                    <span>{readable(item.category)}</span>
                    <span>{dateText(item.createdAt)}</span>
                  </div>
                  {item.adminNote && (
                    <div className="complaint-admin-note">
                      <strong>Hostel team update</strong>
                      <p>{item.adminNote}</p>
                    </div>
                  )}
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </main>
  );
}