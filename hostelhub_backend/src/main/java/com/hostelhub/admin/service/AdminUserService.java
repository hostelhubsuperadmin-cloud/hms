
package com.hostelhub.admin.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.hostelhub.admin.dto.AdminUserResponse;
import com.hostelhub.role.entity.Role;
import com.hostelhub.role.repository.RoleRepository;
import com.hostelhub.security.CustomUserDetails;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminUserService {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminUserService(
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long userId) {
        return toResponse(findUser(userId));
    }

    @Transactional
    public AdminUserResponse assignRole(
            Long userId,
            String roleName,
            CustomUserDetails actor
    ) {
        User target = findUser(userId);
        String normalizedRole = normalizeRoleName(roleName);

        if (SUPER_ADMIN.equals(normalizedRole)
                && !actor.getAuthorities().stream().anyMatch(
                a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            throw new AccessDeniedException(
                    "Only a Super Admin can assign the SUPER_ADMIN role."
            );
        }

        Role role = roleRepository.findByName(normalizedRole)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Role not found: " + normalizedRole
                ));

        boolean alreadyAssigned = target.getRoles().stream()
                .anyMatch(existing ->
                        existing.getName().equals(normalizedRole));

        if (!alreadyAssigned) {
            target.addRole(role);
        }

        return toResponse(target);
    }

    @Transactional
    public AdminUserResponse removeRole(
            Long userId,
            String roleName,
            CustomUserDetails actor
    ) {
        User target = findUser(userId);
        String normalizedRole = normalizeRoleName(roleName);

        if (SUPER_ADMIN.equals(normalizedRole)
                && !actor.getAuthorities().stream().anyMatch(
                a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            throw new AccessDeniedException(
                    "Only a Super Admin can remove the SUPER_ADMIN role."
            );
        }

        Role assignedRole = target.getRoles().stream()
                .filter(role -> role.getName().equals(normalizedRole))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This user does not have the requested role."
                ));

        if (target.getRoles().size() == 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user must retain at least one role."
            );
        }

        if (SUPER_ADMIN.equals(normalizedRole)
                && "ACTIVE".equals(target.getStatus())
                && userRepository.countUsersWithRoleAndStatus(
                SUPER_ADMIN, "ACTIVE") <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot remove the last active Super Admin role."
            );
        }

        target.getRoles().remove(assignedRole);

        return toResponse(target);
    }

    @Transactional
    public AdminUserResponse updateStatus(
            Long userId,
            String requestedStatus,
            CustomUserDetails actor
    ) {
        User target = findUser(userId);
        String newStatus = requestedStatus
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!Set.of("ACTIVE", "SUSPENDED", "DISABLED")
                .contains(newStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be ACTIVE, SUSPENDED, or DISABLED."
            );
        }

        // Do not allow an administrator to disable their own account
        // through this endpoint.
        if (target.getId().equals(actor.getId())
                && !newStatus.equals(target.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You cannot change your own account status here."
            );
        }

        boolean isActiveSuperAdmin =
                target.getRoles().stream()
                        .anyMatch(role ->
                                SUPER_ADMIN.equals(role.getName()))
                        && "ACTIVE".equals(target.getStatus());

        if (isActiveSuperAdmin
                && !"ACTIVE".equals(newStatus)
                && userRepository.countUsersWithRoleAndStatus(
                SUPER_ADMIN, "ACTIVE") <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot deactivate the last active Super Admin."
            );
        }

        target.setStatus(newStatus);

        return toResponse(target);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role name is required."
            );
        }

        String normalized = roleName.trim()
                .toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z][A-Z0-9_]{0,49}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role name."
            );
        }

        return normalized;
    }

    private AdminUserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toUnmodifiableSet());

        return new AdminUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.isEmailVerified(),
                roleNames,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

