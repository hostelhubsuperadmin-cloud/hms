
import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import publicHostelService from "../../services/publicHostelService";
import { getApiError } from "../../api/axiosClient";
import "./PublicHostelPage.css";

export default function PublicHostelsPage() {
  const { id } = useParams();

  const [hostels, setHostels] = useState([]);
  const [hostel, setHostel] = useState(null);
  const [search, setSearch] = useState("");
  const [availability, setAvailability] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
const [menuItems, setMenuItems] = useState([]);
  // Load all active hostels or the selected hostel's details.
  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      setError("");

      // Clear the previous hostel when navigating between details.
      if (id) {
        setHostel(null);
        setAvailability(null);
      } else {
        setHostel(null);
        setAvailability(null);
      }

      try {
        if (id) {
         const [details, beds, menu] = await Promise.all([
  publicHostelService.getById(id),
  publicHostelService.getAvailability(id),
  publicHostelService.getMenu(id),
]);

if (!cancelled) {
  setHostel(details);
  setAvailability(beds);
  setMenuItems(Array.isArray(menu) ? menu : []);
}
        } else {
          // Fetch all hostels. Search filtering happens locally below.
          const results = await publicHostelService.getAll();

          if (!cancelled) {
            setHostels(Array.isArray(results) ? results : []);
          }
        }
      } catch (err) {
        if (!cancelled) {
          setError(getApiError(err));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    load();

    return () => {
      cancelled = true;
    };
  }, [id]);

  // Search by hostel name, address, city, state, country, or postal code.
  const filteredHostels = useMemo(() => {
    const query = search.trim().toLowerCase();

    if (!query) {
      return hostels;
    }

    return hostels.filter((item) => {
      const searchableText = [
        item.name,
        item.description,
        item.addressLine,
        item.city,
        item.state,
        item.country,
        item.postalCode,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return searchableText.includes(query);
    });
  }, [hostels, search]);

  if (loading) {
    return (
      <main className="public-hostels">
        <p>Loading hostels...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="public-hostels">
        <p className="public-error">{error}</p>
        <Link to="/hostels">Back to all hostels</Link>
      </main>
    );
  }

  // Individual hostel details page.
  if (id && hostel) {
    const totalBeds = availability?.totalBeds ?? hostel.totalBeds ?? 0;
    const availableBeds =
      availability?.availableBeds ?? hostel.availableBeds ?? 0;

    return (
      <main className="public-hostels">
        <Link to="/hostels" className="back-link">
          ← All hostels
        </Link>

        <section className="hostel-detail">
          <div className="hostel-detail-heading">
            <div>
              <p className="eyebrow">HOSTEL DETAILS</p>
              <h1>{hostel.name}</h1>

              <p>
                {[
                  hostel.addressLine,
                  hostel.city,
                  hostel.state,
                  hostel.country,
                  hostel.postalCode,
                ]
                  .filter(Boolean)
                  .join(", ")}
              </p>
            </div>

            <span className="availability-badge">
              {availableBeds} beds available
            </span>
          </div>

          <p className="hostel-description">
            {hostel.description ||
              "Contact the hostel for more details."}
          </p>

          <div className="public-stats">
            <article>
              <span>Total beds</span>
              <strong>{totalBeds}</strong>
            </article>

            <article>
              <span>Available beds</span>
              <strong>{availableBeds}</strong>
            </article>
          </div>

          <section className="contact-panel">
            <h2>Contact this hostel</h2>

            {hostel.contactPhone && (
              <p>
                Phone:{" "}
                <a href={`tel:${hostel.contactPhone}`}>
                  {hostel.contactPhone}
                </a>
              </p>
            )}

            {hostel.contactEmail && (
              <p>
                Email:{" "}
                <a href={`mailto:${hostel.contactEmail}`}>
                  {hostel.contactEmail}
                </a>
              </p>
            )}

            {!hostel.contactPhone && !hostel.contactEmail && (
              <p>Contact details have not been published yet.</p>
            )}
          </section>

          {/* The food menu will be displayed here after the
              menu API and hostel-admin menu management are implemented. */}
          
<section className="contact-panel public-menu-panel">
  <h2>Weekly food menu</h2>
  <p className="public-menu-subtitle">
    Food schedule published by this hostel.
  </p>

  {(() => {
    const days = [
      ["MONDAY", "Monday"],
      ["TUESDAY", "Tuesday"],
      ["WEDNESDAY", "Wednesday"],
      ["THURSDAY", "Thursday"],
      ["FRIDAY", "Friday"],
      ["SATURDAY", "Saturday"],
      ["SUNDAY", "Sunday"],
    ];

    const meals = [
      ["BREAKFAST", "Breakfast"],
      ["LUNCH", "Lunch"],
      ["DINNER", "Dinner"],
    ];

    const getMealItems = (day, meal) =>
      menuItems.filter(
        (item) =>
          item.dayOfWeek === day &&
          item.mealType === meal &&
          item.available !== false
      );

    return (
      <div className="public-weekly-menu-wrap">
        <table className="public-weekly-menu">
          <thead>
            <tr>
              <th>Day</th>
              <th>Breakfast</th>
              <th>Lunch</th>
              <th>Dinner</th>
            </tr>
          </thead>

          <tbody>
            {days.map(([dayValue, dayLabel]) => (
              <tr key={dayValue}>
                <th scope="row">{dayLabel}</th>

                {meals.map(([mealValue]) => {
                  const foodItems = getMealItems(dayValue, mealValue);

                  return (
                    <td key={mealValue}>
                      {foodItems.length === 0 ? (
                        <span className="public-menu-not-set">
                          Not published
                        </span>
                      ) : (
                        <ul className="public-menu-food-list">
                          {foodItems.map((item) => (
                            <li key={item.id}>
                              <span>{item.name}</span>
                              {Number(item.price) > 0 && (
                                <small>
                                  ₹{Number(item.price).toFixed(2)}
                                </small>
                              )}
                              {item.description && (
                                <p>{item.description}</p>
                              )}
                            </li>
                          ))}
                        </ul>
                      )}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  })()}
</section>

        </section>
      </main>
    );
  }

  if (id && !hostel) {
    return (
      <main className="public-hostels">
        <p>Hostel details are not available.</p>
        <Link to="/hostels">Back to all hostels</Link>
      </main>
    );
  }

  // Public hostel directory with live search filtering.
  return (
    <main className="public-hostels">
      <header className="public-hero">
        <p className="eyebrow">FIND YOUR NEXT STAY</p>

        <h1>Find a hostel that feels like home.</h1>

        <p>
          Explore hostel locations, check available beds, and contact
          hostels directly.
        </p>

        <label className="city-search">
          <span>Search hostels by name or location</span>

          <input
            type="search"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Enter hostel name, city, or address"
            aria-label="Search hostels by name or location"
          />
        </label>
      </header>

      <section className="hostel-results">
        <div className="results-heading">
          <h2>Available hostels</h2>

          <span>
            {filteredHostels.length}{" "}
            {filteredHostels.length === 1 ? "hostel" : "hostels"} found
          </span>
        </div>

        {hostels.length === 0 ? (
          <p>No active hostels are available right now.</p>
        ) : filteredHostels.length === 0 ? (
          <div className="empty-search">
            <p>No hostels match "{search}".</p>

            <button
              type="button"
              onClick={() => setSearch("")}
            >
              Clear search
            </button>
          </div>
        ) : (
          <div className="hostel-grid">
            {filteredHostels.map((item) => (
              <article className="hostel-card" key={item.id}>
                <div className="hostel-card-image">
                  <span>HOSTELHUB</span>
                </div>

                <div className="hostel-card-content">
                  <h3>{item.name}</h3>

                  <p>
                    {[item.city, item.state]
                      .filter(Boolean)
                      .join(", ")}
                  </p>

                  <p className="hostel-address">
                    {item.addressLine || "Address not published"}
                  </p>

                  <div className="card-availability">
                    <span>
                      {item.availableBeds ?? 0} beds available
                    </span>

                    <span>
                      {item.totalBeds ?? 0} total beds
                    </span>
                  </div>

                  <Link
                    className="details-link"
                    to={`/hostels/${item.id}`}
                  >
                    View hostel details →
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
