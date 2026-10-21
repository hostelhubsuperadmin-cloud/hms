package com.hostelhub.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public boolean hasPermission(
            Authentication authentication,
            String permission
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(permission)
                );
    }
}