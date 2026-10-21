package com.hostelhub.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RoleAuthorityTest {

    private static final Set<String> REQUIRED_ROLES = Set.of(
            "SUPER_ADMIN",
            "HOSTEL_ADMIN",
            "WARDEN",
            "ACCOUNTANT",
            "STAFF",
            "HOSTLER",
            "USER"
    );

    @Test
    void exactlySevenRequiredRolesAreDefined() {
        assertEquals(7, REQUIRED_ROLES.size());
    }

    @Test
    void allRequiredRoleNamesArePresent() {
        assertTrue(REQUIRED_ROLES.containsAll(Set.of(
                "SUPER_ADMIN",
                "HOSTEL_ADMIN",
                "WARDEN",
                "ACCOUNTANT",
                "STAFF",
                "HOSTLER",
                "USER"
        )));
    }

    @Test
    void roleAuthorityNamesUseExpectedPrefix() {
        Set<String> authorities = REQUIRED_ROLES.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertEquals(7, authorities.size());
        assertTrue(authorities.contains("ROLE_SUPER_ADMIN"));
        assertTrue(authorities.contains("ROLE_HOSTEL_ADMIN"));
        assertTrue(authorities.contains("ROLE_WARDEN"));
        assertTrue(authorities.contains("ROLE_ACCOUNTANT"));
        assertTrue(authorities.contains("ROLE_STAFF"));
        assertTrue(authorities.contains("ROLE_HOSTLER"));
        assertTrue(authorities.contains("ROLE_USER"));
    }
}
