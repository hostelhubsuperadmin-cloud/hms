
import { Link } from "react-router-dom";
import { ShieldAlert } from "lucide-react";

export default function UnauthorizedPage() {
  return (
    <div className="app-loading">
      <ShieldAlert size={48} color="#dc5555" />
      <h2>Access restricted</h2>
      <p>Your account does not have access to this page.</p>
      <Link to="/">Return to home</Link>
    </div>
  );
}
