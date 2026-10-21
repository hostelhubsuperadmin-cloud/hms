
export const ROLES = Object.freeze({
  SUPER_ADMIN: "SUPER_ADMIN",
  HOSTEL_ADMIN: "HOSTEL_ADMIN",
  WARDEN: "WARDEN",
  ACCOUNTANT: "ACCOUNTANT",
  STAFF: "STAFF",
  HOSTLER: "HOSTLER",
  USER: "USER",
});

const ROLE_DASHBOARDS = {
  [ROLES.SUPER_ADMIN]: "/super-admin",
  [ROLES.HOSTEL_ADMIN]: "/hostel-admin",
  [ROLES.WARDEN]: "/warden",
  [ROLES.ACCOUNTANT]: "/accountant",
  [ROLES.STAFF]: "/staff",
  [ROLES.HOSTLER]: "/hostler",
  [ROLES.USER]: "/user",
};

export function normalizeRoles(roles) {
  if (!Array.isArray(roles)) return [];

  return roles
    .filter((role) => typeof role === "string")
    .map((role) =>
      role.trim().replace(/^ROLE_/i, "").toUpperCase()
    );
}

export function getUserRoles(user) {
  return normalizeRoles(user?.roles);
}

export function hasAnyRole(user, allowedRoles = []) {
  const currentRoles = getUserRoles(user);
  const allowed = normalizeRoles(allowedRoles);

  return currentRoles.some((role) => allowed.includes(role));
}

export function getDashboardPath(user) {
  const roles = getUserRoles(user);

  const priority = [
    ROLES.SUPER_ADMIN,
    ROLES.HOSTEL_ADMIN,
    ROLES.WARDEN,
    ROLES.ACCOUNTANT,
    ROLES.STAFF,
    ROLES.HOSTLER,
    ROLES.USER,
  ];

  const role = priority.find((candidate) => roles.includes(candidate));

  return ROLE_DASHBOARDS[role] || "/dashboard";
}
