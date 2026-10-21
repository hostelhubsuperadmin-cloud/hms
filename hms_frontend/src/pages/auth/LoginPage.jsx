import { useState } from "react";
import {
  ArrowRight,
  Eye,
  EyeOff,
  LockKeyhole,
  Mail,
  ShieldCheck,
} from "lucide-react";
import {
  Link,
  Navigate,
  useLocation,
  useNavigate,
} from "react-router-dom";

import { useAuth } from "../../auth/AuthContext";
import { getDashboardPath } from "../../utils/roles";
import { getApiError } from "../../api/axiosClient";

import "./auth.css";

export default function LoginPage() {
  const { login, user, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  if (!loading && user) {
    return (
      <Navigate
        to={getDashboardPath(user)}
        replace
      />
    );
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!email.trim() || !password) {
      setError("Enter your email address and password.");
      return;
    }

    setSubmitting(true);

    try {
      const authenticatedUser = await login({
        email,
        password,
      });

      const requestedPath = location.state?.from?.pathname;

      // Role-based redirect is the default. A previous protected
      // destination can be restored after successful authentication.
      navigate(
        requestedPath || getDashboardPath(authenticatedUser),
        { replace: true }
      );
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setSubmitting(false);
    }
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
              Your hostel,
              <br />
              <span>one connected</span>
              <br />
              experience.
            </h1>

            <p className="auth-brand-description">
              A central place for hostel operations, residents,
              room management, and everyday administration.
            </p>

            <ul className="auth-feature-list">
              <li className="auth-feature-item">
                <span className="auth-feature-icon">✓</span>
                Secure account access
              </li>
              <li className="auth-feature-item">
                <span className="auth-feature-icon">⌂</span>
                Organized hostel operations
              </li>
              <li className="auth-feature-item">
                <span className="auth-feature-icon">↗</span>
                Role-based workspaces
              </li>
              <li className="auth-feature-item">
                <span className="auth-feature-icon">◈</span>
                Centralized management
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
              Welcome back
            </h2>

            <p className="auth-form-subtitle">
              Sign in to continue to your HostelHub workspace.
            </p>

            {error && (
              <div
                className="auth-alert auth-alert--error"
                role="alert"
              >
                {error}
              </div>
            )}

            <form
              className="auth-form"
              onSubmit={handleSubmit}
            >
              <div className="auth-field">
                <label
                  className="auth-label"
                  htmlFor="login-email"
                >
                  Email address
                </label>

                <div className="auth-input-wrapper">
                  <span className="auth-input-icon">
                    <Mail size={18} />
                  </span>

                  <input
                    id="login-email"
                    className="auth-input"
                    type="email"
                    name="email"
                    placeholder="you@example.com"
                    autoComplete="username"
                    value={email}
                    onChange={(event) =>
                      setEmail(event.target.value)
                    }
                    required
                    maxLength={255}
                    disabled={submitting}
                  />
                </div>
              </div>

              <div className="auth-field">
                <label
                  className="auth-label"
                  htmlFor="login-password"
                >
                  Password
                </label>

                <div className="auth-input-wrapper">
                  <span className="auth-input-icon">
                    <LockKeyhole size={18} />
                  </span>

                  <input
                    id="login-password"
                    className="auth-input auth-input--password"
                    type={showPassword ? "text" : "password"}
                    name="password"
                    placeholder="Enter your password"
                    autoComplete="current-password"
                    value={password}
                    onChange={(event) =>
                      setPassword(event.target.value)
                    }
                    required
                    disabled={submitting}
                  />

                  <button
                    className="auth-password-toggle"
                    type="button"
                    onClick={() =>
                      setShowPassword((visible) => !visible)
                    }
                    aria-label={
                      showPassword
                        ? "Hide password"
                        : "Show password"
                    }
                    aria-pressed={showPassword}
                  >
                    {showPassword ? (
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
                  Secure sign-in
                </span>

                <Link
                  className="auth-link"
                  to="/forgot-password"
                >
                  Forgot password?
                </Link>
              </div>

              <button
                className="auth-button"
                type="submit"
                disabled={submitting || loading}
              >
                {submitting ? (
                  <>
                    <span className="auth-spinner" />
                    <span className="auth-button-text">
                      Signing in...
                    </span>
                  </>
                ) : (
                  <>
                    <span className="auth-button-text">
                      Sign in to HostelHub
                    </span>
                    <span className="auth-button-icon">
                      <ArrowRight size={18} />
                    </span>
                  </>
                )}
              </button>
            </form>

            <p className="auth-form-bottom">
              New to HostelHub?
              <Link className="auth-link" to="/register">
                Create an account
              </Link>
            </p>

            <div className="auth-security-note">
              <LockKeyhole
                className="auth-security-icon"
                size={14}
              />
              Your authentication token is stored in a
              secure HttpOnly cookie.
            </div>
          </div>
        </section>
      </section>
    </main>
  );
}