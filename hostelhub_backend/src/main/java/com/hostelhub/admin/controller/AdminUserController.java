
package com.hostelhub.admin.controller;

import java.util.List;

import com.hostelhub.admin.dto.AdminUserResponse;
import com.hostelhub.admin.dto.UpdateUserStatusRequest;
import com.hostelhub.admin.service.AdminUserService;
import com.hostelhub.security.CustomUserDetails;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_DIRECTORY_READ')")
    public List<AdminUserResponse> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_DIRECTORY_READ')")
    public AdminUserResponse getUserById(
            @PathVariable Long userId) {
        return adminUserService.getUserById(userId);
    }

    @PutMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public AdminUserResponse assignRole(
            @PathVariable Long userId,
            @PathVariable String roleName,
            @AuthenticationPrincipal CustomUserDetails actor) {

        return adminUserService.assignRole(
                userId, roleName, actor);
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public AdminUserResponse removeRole(
            @PathVariable Long userId,
            @PathVariable String roleName,
            @AuthenticationPrincipal CustomUserDetails actor) {

        return adminUserService.removeRole(
                userId, roleName, actor);
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public AdminUserResponse updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails actor) {

        return adminUserService.updateStatus(
                userId, request.status(), actor);
    }
}

