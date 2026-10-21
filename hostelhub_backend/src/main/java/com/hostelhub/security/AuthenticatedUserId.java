
package com.hostelhub.security;

import org.springframework.security.core.Authentication;

public final class AuthenticatedUserId {

    private AuthenticatedUserId() {
    }

    public static Long from(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        throw new IllegalStateException(
                "Authenticated principal does not contain the HostelHub user ID"
        );
    }
}

