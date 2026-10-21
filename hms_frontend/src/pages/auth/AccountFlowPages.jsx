
import { useEffect, useState } from "react";
import {
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  Eye,
  EyeOff,
  KeyRound,
  LockKeyhole,
  Mail,
  RefreshCw,
  ShieldCheck,
  TriangleAlert,
} from "lucide-react";
import {
  Link,
  useLocation,
  useNavigate,
} from "react-router-dom";

import { useAuth } from "../../auth/AuthContext";
import { getApiError } from "../../api/axiosClient";
import accountService from "../../services/accountService";
import "./account-flow.css";

function AuthShell({
  eyebrow = "HOSTELHUB ACCOUNT SECURITY",
  title,
  description,
  children,
  footer,
}) {
  return (
    <main className="account-page">
      <div className="account-orb account-orb-one" />
      <div className="account-orb account-orb-two" />

      <section className="account-shell">
        <aside className="account-brand">
          <Link className="account-logo" to="/login">
            <span className="account-logo-icon">
              <ShieldCheck size={23} />
            </span>
            <span>Hostel<span className="account-logo-accent">Hub</span></span>
          </Link>

          <div className="account-brand-copy">
            <span className="account-eyebrow">{eyebrow}</span>
            <h2>One secure place for your hostel operations.</h2>
            <p>
              Manage your account with confidence. Your access is protected
              with secure links, verification, and password controls.
            </p>
          </div>

          <div className="account-brand-bottom">
            <span className="account-brand-dot" />
            Secure account access
            <span className="account-brand-separator">·</span>
            HostelHub
          </div>
        </aside>

        <section className="account-content">
          <div className="account-content-inner">
            <div className="account-mobile-logo">
              <span className="account-logo-icon"><ShieldCheck size={20} /></span>
              <strong>HostelHub</strong>
            </div>

            <header className="account-heading">
              <h1>{title}</h1>
              <p>{description}</p>
            </header>

            {children}

            {footer && <footer className="account-footer">{footer}</footer>}
          </div>
        </section>
      </section>
    </main>
  );
}

function Notice({ type = "error", children }) {
  if (!children) return null;

  return (
    <div className={`account-notice account-notice-${type}`} role={type === "error" ? "alert" : "status"}>
      {type === "error"
        ? <TriangleAlert size={18} />
        : <CheckCircle2 size={18} />}
      <span>{children}</span>
    </div>
  );
}

function PasswordField({
  label,
  value,
  onChange,
  autoComplete = "new-password",
  required = true,
}) {
  const [visible, setVisible] = useState(false);

  return (
    <label className="account-field">
      <span>{label}</span>
      <span className="account-input-wrap">
        <LockKeyhole size={18} className="account-input-icon" />
        <input
          type={visible ? "text" : "password"}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          autoComplete={autoComplete}
          minLength={12}
          maxLength={72}
          required={required}
          placeholder="At least 12 characters"
        />
        <button
          className="account-password-toggle"
          type="button"
          onClick={() => setVisible((current) => !current)}
          aria-label={visible ? "Hide password" : "Show password"}
        >
          {visible ? <EyeOff size={18} /> : <Eye size={18} />}
        </button>
      </span>
    </label>
  );
}

function SubmitButton({ loading, children, icon = true }) {
  return (
    <button className="account-submit" type="submit" disabled={loading}>
      {loading ? (
        <>
          <span className="account-spinner" />
          Please wait...
        </>
      ) : (
        <>
          {children}
          {icon && <ArrowRight size={18} />}
        </>
      )}
    </button>
  );
}

function PasswordPair({ password, setPassword, confirm, setConfirm }) {
  return (
    <>
      <PasswordField
        label="New password"
        value={password}
        onChange={setPassword}
      />
      <PasswordField
        label="Confirm new password"
        value={confirm}
        onChange={setConfirm}
      />
      <p className="account-help">
        Use 12–72 characters. A unique passphrase is recommended.
      </p>
    </>
  );
}

function SuccessPanel({ title, description, buttonText = "Continue to sign in" }) {
  return (
    <AuthShell
      title={title}
      description={description}
      eyebrow="ALL SET"
    >
      <div className="account-result">
        <span className="account-result-icon success">
          <CheckCircle2 size={34} />
        </span>
        <h2>{title}</h2>
        <p>{description}</p>
        <Link className="account-submit account-submit-link" to="/login">
          {buttonText} <ArrowRight size={18} />
        </Link>
      </div>
    </AuthShell>
  );
}

export function ActivateAccountPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const token = new URLSearchParams(location.search).get("token") || "";

  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!token.trim()) {
      navigate("/activation-expired?reason=missing", { replace: true });
    }
  }, [token, navigate]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (password.length < 12 || password.length > 72) {
      setError("Your password must be between 12 and 72 characters.");
      return;
    }
    if (password !== confirm) {
      setError("The passwords do not match.");
      return;
    }

    setLoading(true);
    try {
      await accountService.activateAccount({
        token,
        newPassword: password,
        confirmPassword: confirm,
      });
      navigate("/activation-success", { replace: true });
    } catch (err) {
      const message = getApiError(err);
      const status = err?.response?.status;
      const lower = message.toLowerCase();

      if (
        status === 410 ||
        lower.includes("expired") ||
        lower.includes("invalid token") ||
        lower.includes("invalid or expired") ||
        lower.includes("already used")
      ) {
        navigate("/activation-expired", { replace: true });
      } else {
        setError(message);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="Set up your account"
      description="Choose a secure password to activate your HostelHub account."
    >
      <div className="account-step">
        <span className="account-step-icon"><KeyRound size={20} /></span>
        <div>
          <strong>Final step</strong>
          <p>Your activation link is personal. Do not share it with anyone.</p>
        </div>
      </div>

      <form className="account-form" onSubmit={handleSubmit}>
        <Notice>{error}</Notice>
        <PasswordPair
          password={password}
          setPassword={setPassword}
          confirm={confirm}
          setConfirm={setConfirm}
        />
        <SubmitButton loading={loading}>Activate account</SubmitButton>
      </form>

      <p className="account-inline-link">
        Link not working? <Link to="/resend-activation">Request a new activation email</Link>
      </p>
    </AuthShell>
  );
}

export function ActivationExpiredPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    setLoading(true);

    try {
      const result = await accountService.resendActivation(email);
      setMessage(result?.message || "If activation is required, an email will be sent.");
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="This link may have expired"
      description="Activation links are time-limited. Request a new link to continue."
    >
      <div className="account-result account-result-compact">
        <span className="account-result-icon warning">
          <TriangleAlert size={32} />
        </span>
      </div>

      <form className="account-form" onSubmit={handleSubmit}>
        <Notice>{error}</Notice>
        <Notice type="success">{message}</Notice>

        <label className="account-field">
          <span>Email address</span>
          <span className="account-input-wrap">
            <Mail size={18} className="account-input-icon" />
            <input
              type="email"
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              required
              maxLength={254}
            />
          </span>
        </label>

        <SubmitButton loading={loading}>Send a new link</SubmitButton>
      </form>

      <p className="account-inline-link">
        <Link to="/login"><ArrowLeft size={15} /> Back to sign in</Link>
      </p>
    </AuthShell>
  );
}

export function ActivationSuccessPage() {
  return (
    <SuccessPanel
      title="Account activated!"
      description="Your password has been set and your account is ready. You can now sign in securely."
    />
  );
}

export function ResendActivationPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");

    try {
      const result = await accountService.resendActivation(email);
      setMessage(result?.message || "If activation is required, an email will be sent.");
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="Resend activation email"
      description="Enter the email address that your hostel administrator used to invite you."
    >
      <form className="account-form" onSubmit={handleSubmit}>
        <Notice>{error}</Notice>
        <Notice type="success">{message}</Notice>

        <label className="account-field">
          <span>Email address</span>
          <span className="account-input-wrap">
            <Mail size={18} className="account-input-icon" />
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              autoComplete="email"
              maxLength={254}
              required
            />
          </span>
        </label>

        <SubmitButton loading={loading}>
          <RefreshCw size={17} /> Send activation link
        </SubmitButton>
      </form>
      <p className="account-inline-link">
        <Link to="/login"><ArrowLeft size={15} /> Back to sign in</Link>
      </p>
    </AuthShell>
  );
}

export function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");

    try {
      const result = await accountService.requestPasswordOtp(email);
      setMessage(
        result?.message ||
        "If an eligible account exists, a password-reset OTP will be sent."
      );
      navigate("/verify-reset-otp", {
        state: { email: email.trim(), notice: result?.message },
      });
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="Forgot your password?"
      description="Enter your account email and we’ll send a one-time verification code."
    >
      <div className="account-step">
        <span className="account-step-icon"><Mail size={20} /></span>
        <div>
          <strong>Email verification</strong>
          <p>The OTP expires after a short time and can only be used within the configured limit.</p>
        </div>
      </div>

      <form className="account-form" onSubmit={handleSubmit}>
        <Notice>{error}</Notice>
        <Notice type="success">{message}</Notice>

        <label className="account-field">
          <span>Email address</span>
          <span className="account-input-wrap">
            <Mail size={18} className="account-input-icon" />
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              autoComplete="email"
              maxLength={254}
              required
            />
          </span>
        </label>

        <SubmitButton loading={loading}>Send verification code</SubmitButton>
      </form>

      <p className="account-inline-link">
        <Link to="/login"><ArrowLeft size={15} /> Back to sign in</Link>
      </p>
    </AuthShell>
  );
}

export function VerifyResetOtpPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [email, setEmail] = useState(location.state?.email || "");
  const [otp, setOtp] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!/^\d{6}$/.test(otp)) {
      setError("Enter the six-digit code from your email.");
      return;
    }

    setLoading(true);
    try {
      const result = await accountService.verifyPasswordOtp({ email, otp });
      if (!result?.resetToken) {
        throw new Error("The server did not return a reset token. Please request a new code.");
      }
      navigate("/reset-password", {
        replace: true,
        state: { resetToken: result.resetToken },
      });
    } catch (err) {
      setError(err?.response ? getApiError(err) : err.message || "Could not verify this code.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="Verify your email"
      description="Enter the six-digit OTP sent to your email address."
    >
      <form className="account-form" onSubmit={handleSubmit}>
        <Notice>{error}</Notice>

        <label className="account-field">
          <span>Email address</span>
          <span className="account-input-wrap">
            <Mail size={18} className="account-input-icon" />
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              placeholder="you@example.com"
              maxLength={254}
              required
            />
          </span>
        </label>

        <label className="account-field">
          <span>6-digit verification code</span>
          <input
            className="account-otp-input"
            inputMode="numeric"
            autoComplete="one-time-code"
            value={otp}
            onChange={(event) => setOtp(event.target.value.replace(/\D/g, "").slice(0, 6))}
            placeholder="000000"
            maxLength={6}
            pattern="\d{6}"
            required
          />
        </label>

        <SubmitButton loading={loading}>Verify code</SubmitButton>
      </form>

      <p className="account-inline-link">
        Didn’t receive a code? <Link to="/forgot-password">Request another OTP</Link>
      </p>
    </AuthShell>
  );
}

export function ResetPasswordPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const resetToken = location.state?.resetToken || "";

  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  if (!resetToken) {
    return (
      <AuthShell
        title="Verification required"
        description="Verify your email OTP before creating a new password."
      >
        <Notice>For your security, this page needs the reset token returned by OTP verification.</Notice>
        <Link className="account-submit account-submit-link" to="/forgot-password">
          Start password recovery <ArrowRight size={18} />
        </Link>
      </AuthShell>
    );
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (password.length < 12 || password.length > 72) {
      setError("Your password must be between 12 and 72 characters.");
      return;
    }
    if (password !== confirm) {
      setError("The passwords do not match.");
      return;
    }

    setLoading(true);
    try {
      await accountService.resetPassword({
        resetToken,
        newPassword: password,
        confirmPassword: confirm,
      });
      navigate("/password-reset-success", { replace: true });
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="Create a new password"
      description="Choose a new password that you have not used for this account before."
    >
      <form className="account-form" onSubmit={handleSubmit}>
        <Notice>{error}</Notice>
        <PasswordPair
          password={password}
          setPassword={setPassword}
          confirm={confirm}
          setConfirm={setConfirm}
        />
        <SubmitButton loading={loading}>Reset password</SubmitButton>
      </form>
    </AuthShell>
  );
}

export function PasswordResetSuccessPage() {
  return (
    <SuccessPanel
      title="Password reset complete"
      description="Your password has been updated. Sign in with your new password to continue."
    />
  );
}

export function ChangePasswordPage() {
  const { user } = useAuth();
  const [currentPassword, setCurrentPassword] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (password.length < 12 || password.length > 72) {
      setError("Your new password must be between 12 and 72 characters.");
      return;
    }
    if (password !== confirm) {
      setError("The new passwords do not match.");
      return;
    }
    if (currentPassword === password) {
      setError("Choose a new password different from your current password.");
      return;
    }

    setLoading(true);
    try {
      const result = await accountService.changePassword({
        currentPassword,
        newPassword: password,
        confirmPassword: confirm,
      });
      setMessage(result?.message || "Password changed successfully.");
      setCurrentPassword("");
      setPassword("");
      setConfirm("");
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="Change password"
      description="Update the password for your signed-in HostelHub account."
      eyebrow={user?.email || "ACCOUNT SETTINGS"}
    >
      <form className="account-form" onSubmit={handleSubmit}>
        <Notice>{error}</Notice>
        <Notice type="success">{message}</Notice>

        <PasswordField
          label="Current password"
          value={currentPassword}
          onChange={setCurrentPassword}
          autoComplete="current-password"
        />
        <PasswordPair
          password={password}
          setPassword={setPassword}
          confirm={confirm}
          setConfirm={setConfirm}
        />
        <SubmitButton loading={loading}>Update password</SubmitButton>
      </form>
    </AuthShell>
  );
}
