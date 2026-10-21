
package com.hostelhub.hostel.controller;

import com.hostelhub.hostel.dto.CreateHostelAdminRequest;
import com.hostelhub.hostel.dto.HostelAdminAssignmentResponse;
import com.hostelhub.hostel.dto.HostelAdminResponse;
import com.hostelhub.hostel.service.HostelAdminManagementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/hostel-admins")
@PreAuthorize("hasAuthority('HOSTEL_MANAGE')")
public class SuperAdminHostelAdminController {

    private final HostelAdminManagementService service;

    public SuperAdminHostelAdminController(
            HostelAdminManagementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HostelAdminResponse create(
            @Valid @RequestBody CreateHostelAdminRequest request) {
        return service.createAdmin(request);
    }

    @GetMapping
    public List<HostelAdminResponse> list() {
        return service.listAdmins();
    }

    @PostMapping("/{adminId}/assign/{hostelId}")
    public HostelAdminAssignmentResponse assign(
            @PathVariable Long adminId,
            @PathVariable Long hostelId,
            Authentication authentication) {

        return service.assignAdmin(
                adminId,
                hostelId,
                authentication.getName()
        );
    }
    @DeleteMapping("/{adminId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAdmin(
            @PathVariable Long adminId,
            Authentication authentication) {

        service.deleteAdmin(adminId, authentication.getName());
    }
    @GetMapping("/{adminId}/assignments")
    public List<HostelAdminAssignmentResponse> listAssignments(
            @PathVariable Long adminId) {
        return service.listAdminAssignments(adminId);
    }

    @DeleteMapping("/{adminId}/assign/{hostelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassign(
            @PathVariable Long adminId,
            @PathVariable Long hostelId) {
        service.unassignAdminFromHostel(adminId, hostelId);
    }
}

