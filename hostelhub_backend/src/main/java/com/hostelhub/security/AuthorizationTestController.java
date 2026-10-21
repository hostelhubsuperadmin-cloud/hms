
package com.hostelhub.security;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authorization-test")
public class AuthorizationTestController {

    @GetMapping("/me")
    public Map<String, Object> currentAuthorities(
            @AuthenticationPrincipal CustomUserDetails principal) {

        Set<String> authorities = principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Map.of(
                "userId", principal.getId(),
                "email", principal.getUsername(),
                "roles", principal.getRoleNames(),
                "authorities", authorities
        );
    }

    @GetMapping("/super-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Map<String, String> superAdminOnly() {
        return Map.of(
                "message",
                "Access granted to SUPER_ADMIN."
        );
    }

    @GetMapping("/hostel-management")
    @PreAuthorize("hasAuthority('HOSTEL_MANAGE')")
    public Map<String, String> hostelManagement() {
        return Map.of(
                "message",
                "HOSTEL_MANAGE permission verified."
        );
    }

    @GetMapping("/structure-management")
    @PreAuthorize("hasAuthority('HOSTEL_STRUCTURE_MANAGE')")
    public Map<String, String> structureManagement() {
        return Map.of(
                "message",
                "HOSTEL_STRUCTURE_MANAGE permission verified."
        );
    }

    @GetMapping("/payment-management")
    @PreAuthorize("hasAuthority('PAYMENT_MANAGE')")
    public Map<String, String> paymentManagement() {
        return Map.of(
                "message",
                "PAYMENT_MANAGE permission verified."
        );
    }

    @GetMapping("/role-management")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Map<String, String> roleManagement() {
        return Map.of(
                "message",
                "ROLE_MANAGE permission verified."
        );
    }
}

