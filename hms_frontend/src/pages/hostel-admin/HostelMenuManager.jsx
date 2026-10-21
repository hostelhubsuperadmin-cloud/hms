import { useCallback, useEffect, useState } from "react";
import { Pencil, Plus, Trash2, Utensils, X } from "lucide-react";
import hostelAdminService from "../../services/hostelAdminService";
import { getApiError } from "../../api/axiosClient";
import "./HostelMenuManager.css";

const DAYS = [
  ["MONDAY", "Monday"],
  ["TUESDAY", "Tuesday"],
  ["WEDNESDAY", "Wednesday"],
  ["THURSDAY", "Thursday"],
  ["FRIDAY", "Friday"],
  ["SATURDAY", "Saturday"],
  ["SUNDAY", "Sunday"],
];

const MEALS = [
  ["BREAKFAST", "Breakfast"],
  ["LUNCH", "Lunch"],
  ["DINNER", "Dinner"],
];

const EMPTY_FORM = {
  dayOfWeek: "MONDAY",
  mealType: "BREAKFAST",
  name: "",
  description: "",
  price: "0",
  available: true,
};

function messageOf(error) {
  return (
    getApiError?.(error) ||
    error?.response?.data?.message ||
    error?.message ||
    "Something went wrong."
  );
}

export default function HostelMenuManager({ hostelId }) {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [selectedDay, setSelectedDay] = useState("MONDAY");
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const loadItems = useCallback(async () => {
    if (!hostelId) return;

    setLoading(true);
    setError("");

    try {
      const data = await hostelAdminService.getMenu(hostelId);
      setItems(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(messageOf(err));
    } finally {
      setLoading(false);
    }
  }, [hostelId]);

  useEffect(() => {
    loadItems();
  }, [loadItems]);

  function openCreate(day = selectedDay, mealType = "BREAKFAST") {
    setEditingId(null);
    setForm({
      ...EMPTY_FORM,
      dayOfWeek: day,
      mealType,
    });
    setError("");
    setNotice("");
    setShowForm(true);
  }

  function openEdit(item) {
    setEditingId(item.id);
    setForm({
      dayOfWeek: item.dayOfWeek,
      mealType: item.mealType,
      name: item.name || "",
      description: item.description || "",
      price: String(item.price ?? 0),
      available: Boolean(item.available),
    });
    setError("");
    setNotice("");
    setShowForm(true);
  }

  function closeForm() {
    setShowForm(false);
    setEditingId(null);
    setForm(EMPTY_FORM);
  }

  async function submit(event) {
    event.preventDefault();

    const price = Number(form.price);

    if (!form.name.trim()) {
      setError("Enter a food item name.");
      return;
    }

    if (!Number.isFinite(price) || price < 0) {
      setError("Enter a valid non-negative price.");
      return;
    }

    setSaving(true);
    setError("");
    setNotice("");

    const payload = {
      dayOfWeek: form.dayOfWeek,
      mealType: form.mealType,
      name: form.name.trim(),
      description: form.description.trim(),
      price,
      available: form.available,
    };

    try {
      if (editingId !== null) {
        await hostelAdminService.updateMenuItem(
          hostelId,
          editingId,
          payload
        );
        setNotice("Food item updated successfully.");
      } else {
        await hostelAdminService.createMenuItem(hostelId, payload);
        setNotice("Food item added successfully.");
      }

      closeForm();
      await loadItems();
    } catch (err) {
      setError(messageOf(err));
    } finally {
      setSaving(false);
    }
  }

  async function removeItem(item) {
    const confirmed = window.confirm(
      `Delete "${item.name}" from the weekly menu?`
    );

    if (!confirmed) return;

    setError("");
    setNotice("");

    try {
      await hostelAdminService.deleteMenuItem(hostelId, item.id);
      setNotice("Food item deleted successfully.");
      await loadItems();
    } catch (err) {
      setError(messageOf(err));
    }
  }

  const dayItems = items.filter(
    (item) => item.dayOfWeek === selectedDay
  );

  return (
    <section className="hmm">
      <header className="hmm-header">
        <div>
          <span className="hmm-eyebrow">HOSTEL OPERATIONS</span>
          <h2>
            <Utensils size={23} />
            Weekly food menu
          </h2>
          <p>
            Define each day's breakfast, lunch, and dinner for this hostel.
          </p>
        </div>

        <button
          type="button"
          className="hmm-primary"
          onClick={() => openCreate()}
        >
          <Plus size={17} />
          Add food item
        </button>
      </header>

      {error && (
        <div className="hmm-alert hmm-error" role="alert">
          <span>{error}</span>
          <button type="button" onClick={() => setError("")}>
            <X size={16} />
          </button>
        </div>
      )}

      {notice && (
        <div className="hmm-alert hmm-success" role="status">
          <span>{notice}</span>
          <button type="button" onClick={() => setNotice("")}>
            <X size={16} />
          </button>
        </div>
      )}

      <div className="hmm-days" aria-label="Choose weekday">
        {DAYS.map(([value, label]) => (
          <button
            type="button"
            key={value}
            className={selectedDay === value ? "selected" : ""}
            onClick={() => setSelectedDay(value)}
          >
            {label}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="hmm-empty">Loading weekly menu…</p>
      ) : (
        <div className="hmm-meals">
          {MEALS.map(([mealValue, mealLabel]) => {
            const mealItems = dayItems.filter(
              (item) => item.mealType === mealValue
            );

            return (
              <section className="hmm-meal-card" key={mealValue}>
                <div className="hmm-meal-heading">
                  <div>
                    <h3>{mealLabel}</h3>
                    <span>
                      {mealItems.length} food item
                      {mealItems.length === 1 ? "" : "s"}
                    </span>
                  </div>

                  <button
                    type="button"
                    className="hmm-add-small"
                    onClick={() => openCreate(selectedDay, mealValue)}
                  >
                    <Plus size={15} />
                    Add
                  </button>
                </div>

                {mealItems.length === 0 ? (
                  <p className="hmm-empty-meal">
                    No {mealLabel.toLowerCase()} items defined for{" "}
                    {DAYS.find(([day]) => day === selectedDay)?.[1]}.
                  </p>
                ) : (
                  <div className="hmm-item-list">
                    {mealItems.map((item) => (
                      <article className="hmm-item" key={item.id}>
                        <div className="hmm-item-main">
                          <strong>{item.name}</strong>
                          {item.description && (
                            <p>{item.description}</p>
                          )}
                          <span
                            className={
                              item.available
                                ? "hmm-status available"
                                : "hmm-status unavailable"
                            }
                          >
                            {item.available ? "Available publicly" : "Hidden publicly"}
                          </span>
                        </div>

                        <div className="hmm-item-actions">
                          <span className="hmm-price">
                            ₹{Number(item.price || 0).toFixed(2)}
                          </span>
                          <button
                            type="button"
                            aria-label={`Edit ${item.name}`}
                            onClick={() => openEdit(item)}
                          >
                            <Pencil size={16} />
                          </button>
                          <button
                            type="button"
                            aria-label={`Delete ${item.name}`}
                            onClick={() => removeItem(item)}
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </section>
            );
          })}
        </div>
      )}

      {showForm && (
        <div
          className="hmm-modal-backdrop"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget && !saving) closeForm();
          }}
        >
          <section
            className="hmm-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="hmm-form-title"
          >
            <header>
              <div>
                <h3 id="hmm-form-title">
                  {editingId !== null ? "Edit food item" : "Add food item"}
                </h3>
                <p>Choose the day and meal for this item.</p>
              </div>
              <button
                type="button"
                aria-label="Close form"
                disabled={saving}
                onClick={closeForm}
              >
                <X size={19} />
              </button>
            </header>

            <form onSubmit={submit}>
              <label>
                Day
                <select
                  value={form.dayOfWeek}
                  onChange={(event) =>
                    setForm({ ...form, dayOfWeek: event.target.value })
                  }
                >
                  {DAYS.map(([value, label]) => (
                    <option value={value} key={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Meal
                <select
                  value={form.mealType}
                  onChange={(event) =>
                    setForm({ ...form, mealType: event.target.value })
                  }
                >
                  {MEALS.map(([value, label]) => (
                    <option value={value} key={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Food item name
                <input
                  required
                  maxLength={120}
                  value={form.name}
                  onChange={(event) =>
                    setForm({ ...form, name: event.target.value })
                  }
                  placeholder="e.g. Idly"
                />
              </label>

              <label>
                Description / ingredients (optional)
                <textarea
                  rows={3}
                  maxLength={1000}
                  value={form.description}
                  onChange={(event) =>
                    setForm({ ...form, description: event.target.value })
                  }
                  placeholder="e.g. Served with chutney"
                />
              </label>

              <label>
                Price (₹; use 0 if included in hostel fees)
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  required
                  value={form.price}
                  onChange={(event) =>
                    setForm({ ...form, price: event.target.value })
                  }
                />
              </label>

              <label className="hmm-checkbox">
                <input
                  type="checkbox"
                  checked={form.available}
                  onChange={(event) =>
                    setForm({ ...form, available: event.target.checked })
                  }
                />
                Show this item on the public menu
              </label>

              {error && <p className="hmm-form-error">{error}</p>}

              <footer>
                <button
                  type="button"
                  className="hmm-secondary"
                  disabled={saving}
                  onClick={closeForm}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="hmm-primary"
                  disabled={saving}
                >
                  {saving ? "Saving…" : editingId !== null ? "Save changes" : "Add item"}
                </button>
              </footer>
            </form>
          </section>
        </div>
      )}
    </section>
  );
}
