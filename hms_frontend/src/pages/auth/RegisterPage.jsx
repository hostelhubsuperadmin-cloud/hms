
import { useState } from "react";
import {
  ArrowRight,
  Eye,
  EyeOff,
  LockKeyhole,
  Mail,
  UserRound,
  Phone,
  ShieldCheck,
  CheckCircle2,
} from "lucide-react";
import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../../auth/AuthContext";
import { getApiError } from "../../api/axiosClient";

import "./auth.css";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    if (error) setError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    const fullName = form.fullName.trim();
    const email = form.email.trim();

    if (!fullName || !email || !form.password) {
      setError("Please complete all required fields.");
      return;
    }

    if (form.password.length < 12 || form.password.length > 72) {
      setError("Password must contain between 12 and 72 characters.");
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setSubmitting(true);

    try {
      await register({
        fullName,
        email,
        phone: form.phone.trim() || null,
        password: form.password,
      });

      setSuccess(true);
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setSubmitting(false);
    }
  }

  if (success) {
    return (
      <main className="auth-page">
        <div className="auth-bg-orb auth-bg-orb--one" />
        <div className="auth-bg-orb auth-bg-orb--two" />

        <section className="auth-container">
          <aside className="auth-brand-panel">
            <div className="auth-brand-content">
              <div className="auth-brand-logo">
                <span className="auth-logo-mark">H</span>
                <span>HostelHub</span>
              </div>

              <div className="auth-brand-eyebrow">
                <span className="auth-brand-eyebrow-dot" />
                Smart Hostel Management
              </div>

              <h1 className="auth-brand-title">
                Your journey
                <br />
                <span>starts here.</span>
              </h1>

              <p className="auth-brand-description">
                Your account has been submitted. Complete account
                activation before signing in.
              </p>
            </div>

            <div className="auth-brand-footer">
              Built for a simpler, more connected hostel experience.
            </div>
          </aside>

          <section className="auth-form-panel">
            <div className="auth-form-wrapper">
              <div
                style={{
                  width: 58,
                  height: 58,
                  display: "grid",
                  placeItems: "center",
                  borderRadius: 18,
                  background: "#eafaf2",
                  color: "#168653",
                  marginBottom: 22,
                }}
              >
                <CheckCircle2 size={30} />
              </div>

              <h2 className="auth-form-heading">
                Registration successful
              </h2>

              <p className="auth-form-subtitle">
                Your account was created successfully.
              </p>

              <div
                className="auth-alert"
                role="status"
                style={{
                  padding: 16,
                  lineHeight: 1.7,
                  marginTop: 22,
                }}
              >
                <strong>Next step: activate your account.</strong>
                <br />
                Check your email for the activation link sent by
                HostelHub. Open that link before attempting to sign in.
              </div>

              <button
                type="button"
                className="auth-button"
                style={{ marginTop: 24 }}
                onClick={() => navigate("/login", { replace: true })}
              >
                <span className="auth-button-text">
                  Continue to sign in
                </span>
                <span className="auth-button-icon">
                  <ArrowRight size={18} />
                </span>
              </button>

              <p className="auth-form-bottom">
                Didn't receive the email?
                <Link
                  className="auth-link"
                  to="/resend-activation"
                >
                  Resend activation
                </Link>
              </p>
            </div>
          </section>
        </section>
      </main>
    );
  }

  return (
    <main className="auth-page">
      <div className="auth-bg-orb auth-bg-orb--one" />
      <div className="auth-bg-orb auth-bg-orb--two" />
      <div className="auth-bg-orb auth-bg-orb--three" />

      <section className="auth-container">
        <aside className="auth-brand-panel">
          <div className="auth-brand-content">
            <div className="auth-brand-logo">
              <span className="auth-logo-mark">H</span>
              <span>HostelHub</span>
            </div>

            <div className="auth-brand-eyebrow">
              <span className="auth-brand-eyebrow-dot" />
              Smart Hostel Management
            </div>

            <h1 className="auth-brand-title">
              A smarter
              <br />
              <span>way to manage</span>
              <br />
              hostel life.
            </h1>

            <p className="auth-brand-description">
              Create your account to get started with a connected
              hostel management experience.
            </p>

            <ul className="auth-feature-list">
              <li className="auth-feature-item">
                <span className="auth-feature-icon">✓</span>
                Simple account registration
              </li>
              <li className="auth-feature-item">
                <span className="auth-feature-icon">⌂</span>
                Organized hostel operations
              </li>
              <li className="auth-feature-item">
                <span className="auth-feature-icon">↗</span>
                Secure account activation
              </li>
              <li className="auth-feature-item">
                <span className="auth-feature-icon">◈</span>
                Role-based access
              </li>
            </ul>
          </div>

          <div className="auth-brand-footer">
            Built for a simpler, more connected hostel experience.
          </div>
        </aside>

        <section className="auth-form-panel">
          <div className="auth-form-wrapper">
            <h2 className="auth-form-heading">
              Create your account
            </h2>

            <p className="auth-form-subtitle">
              Enter your details to register with HostelHub.
            </p>

            {error && (
              <div
                className="auth-alert auth-alert--error"
                role="alert"
              >
                {error}
              </div>
            )}

            <form className="auth-form" onSubmit={handleSubmit}>
              <div className="auth-field">
                <label className="auth-label" htmlFor="register-name">
                  Full name
                </label>

                <div className="auth-input-wrapper">
                  <span className="auth-input-icon">
                    <UserRound size={18} />
                  </span>

                  <input
                    id="register-name"
                    className="auth-input"
                    type="text"
                    name="fullName"
                    placeholder="Enter your full name"
                    autoComplete="name"
                    value={form.fullName}
                    onChange={handleChange}
                    required
                    maxLength={150}
                    disabled={submitting}
                  />
                </div>
              </div>

              <div className="auth-field">
                <label className="auth-label" htmlFor="register-email">
                  Email address
                </label>

                <div className="auth-input-wrapper">
                  <span className="auth-input-icon">
                    <Mail size={18} />
                  </span>

                  <input
                    id="register-email"
                    className="auth-input"
                    type="email"
                    name="email"
                    placeholder="you@example.com"
                    autoComplete="email"
                    value={form.email}
                    onChange={handleChange}
                    required
                    maxLength={255}
                    disabled={submitting}
                  />
                </div>
              </div>

              <div className="auth-field">
                <label className="auth-label" htmlFor="register-phone">
                  Phone number <span>(optional)</span>
                </label>

                <div className="auth-input-wrapper">
                  <span className="auth-input-icon">
                    <Phone size={18} />
                  </span>

                  <input
                    id="register-phone"
                    className="auth-input"
                    type="tel"
                    name="phone"
                    placeholder="Enter your phone number"
                    autoComplete="tel"
                    value={form.phone}
                    onChange={handleChange}
                    maxLength={20}
                    disabled={submitting}
                  />
                </div>
              </div>

              <div className="auth-field">
                <label
                  className="auth-label"
                  htmlFor="register-password"
                >
                  Password
                </label>

                <div className="auth-input-wrapper">
                  <span className="auth-input-icon">
                    <LockKeyhole size={18} />
                  </span>

                  <input
                    id="register-password"
                    className="auth-input auth-input--password"
                    type={showPassword ? "text" : "password"}
                    name="password"
                    placeholder="At least 12 characters"
                    autoComplete="new-password"
                    value={form.password}
                    onChange={handleChange}
                    required
                    minLength={12}
                    maxLength={72}
                    disabled={submitting}
                  />

                  <button
                    className="auth-password-toggle"
                    type="button"
                    onClick={() =>
                      setShowPassword((visible) => !visible)
                    }
                    aria-label={
                      showPassword ? "Hide password" : "Show password"
                    }
                  >
                    {showPassword ? (
                      <EyeOff size={18} />
                    ) : (
                      <Eye size={18} />
                    )}
                  </button>
                </div>
              </div>

              <div className="auth-field">
                <label
                  className="auth-label"
                  htmlFor="register-confirm-password"
                >
                  Confirm password
                </label>

                <div className="auth-input-wrapper">
                  <span className="auth-input-icon">
                    <ShieldCheck size={18} />
                  </span>

                  <input
                    id="register-confirm-password"
                    className="auth-input auth-input--password"
                    type={showConfirmPassword ? "text" : "password"}
                    name="confirmPassword"
                    placeholder="Re-enter your password"
                    autoComplete="new-password"
                    value={form.confirmPassword}
                    onChange={handleChange}
                    required
                    maxLength={72}
                    disabled={submitting}
                  />

                  <button
                    className="auth-password-toggle"
                    type="button"
                    onClick={() =>
                      setShowConfirmPassword((visible) => !visible)
                    }
                    aria-label={
                      showConfirmPassword
                        ? "Hide confirm password"
                        : "Show confirm password"
                    }
                  >
                    {showConfirmPassword ? (
                      <EyeOff size={18} />
                    ) : (
                      <Eye size={18} />
                    )}
                  </button>
                </div>
              </div>

              <div className="auth-form-options">
                <span className="auth-checkbox-label">
                  <ShieldCheck size={15} />
                  Secure registration
                </span>
              </div>

              <button
                className="auth-button"
                type="submit"
                disabled={submitting}
              >
                {submitting ? (
                  <>
                    <span className="auth-spinner" />
                    <span className="auth-button-text">
                      Creating account...
                    </span>
                  </>
                ) : (
                  <>
                    <span className="auth-button-text">
                      Create account
                    </span>
                    <span className="auth-button-icon">
                      <ArrowRight size={18} />
                    </span>
                  </>
                )}
              </button>
            </form>

            <p className="auth-form-bottom">
              Already registered?
              <Link className="auth-link" to="/login">
                Sign in
              </Link>
            </p>

            <div className="auth-security-note">
              <LockKeyhole className="auth-security-icon" size={14} />
              Your account must be activated before you can sign in.
            </div>
          </div>
        </section>
      </section>
    </main>
  );
}
