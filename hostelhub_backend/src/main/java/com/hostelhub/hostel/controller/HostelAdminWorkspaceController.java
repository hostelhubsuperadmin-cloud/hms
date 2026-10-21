
package com.hostelhub.hostel.controller;

import com.hostelhub.hostel.dto.HostelAdminAssignmentResponse;
import com.hostelhub.hostel.service.HostelAdminManagementService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostel-admin")
public class HostelAdminWorkspaceController {

    private final HostelAdminManagementService service;

    public HostelAdminWorkspaceController(
            HostelAdminManagementService service) {
        this.service = service;
    }

    @GetMapping("/my-hostels")
    @PreAuthorize("hasRole('HOSTEL_ADMIN')")
    public List<HostelAdminAssignmentResponse> myHostels(
            Authentication authentication) {
        return service.myHostels(authentication.getName());
    }
}

