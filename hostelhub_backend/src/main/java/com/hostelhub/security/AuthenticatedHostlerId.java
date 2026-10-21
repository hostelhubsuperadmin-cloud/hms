package com.hostelhub.security;

import org.springframework.security.core.Authentication;

public final class AuthenticatedHostlerId {

    private AuthenticatedHostlerId() {
    }

    public static Long from(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException(
                    "User is not authenticated."
            );
        }

        if (!userDetails.getRoleNames().contains("HOSTLER")) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only hostlers can access their own billing records."
            );
        }

        // Existing allocation, invoice and payment tables use users.id
        // as hostler_id. The hostler profile ID is the same user ID.
        return userDetails.getId();
    }
}

