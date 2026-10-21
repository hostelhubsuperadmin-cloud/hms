package com.hostelhub.complaint.controller;

import com.hostelhub.complaint.dto.ComplaintResponse;
import com.hostelhub.complaint.dto.CreateComplaintRequest;
import com.hostelhub.complaint.dto.UpdateComplaintRequest;
import com.hostelhub.complaint.service.ComplaintService;
import com.hostelhub.security.AuthenticatedHostlerId;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service) {
        this.service = service;
    }

    // Resident: submit a complaint
    @PostMapping("/hostler/complaints")
    @PreAuthorize("hasRole('HOSTLER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ComplaintResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateComplaintRequest request
    ) {
        Long hostlerId = AuthenticatedHostlerId.from(authentication);
        return service.create(hostlerId, request);
    }

    // Resident: see only their own complaints
    @GetMapping("/hostler/complaints")
    @PreAuthorize("hasRole('HOSTLER')")
    public List<ComplaintResponse> myComplaints(
            Authentication authentication
    ) {
        Long hostlerId = AuthenticatedHostlerId.from(authentication);
        return service.getMyComplaints(hostlerId);
    }

    @GetMapping("/hostler/complaints/{id}")
    @PreAuthorize("hasRole('HOSTLER')")
    public ComplaintResponse myComplaint(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long hostlerId = AuthenticatedHostlerId.from(authentication);
        return service.getMyComplaint(hostlerId, id);
    }

    // Hostel admin or warden: list complaints for assigned hostel
    @GetMapping("/hostel-admin/hostels/{hostelId}/complaints")
    @PreAuthorize("hasAnyRole('HOSTEL_ADMIN', 'WARDEN')")
    public List<ComplaintResponse> hostelComplaints(
            Authentication authentication,
            @PathVariable Long hostelId,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        return service.getHostelComplaints(
                authentication.getName(), hostelId, status
        );
    }

    // Hostel admin or warden: update complaint status and note
    @PatchMapping(
            "/hostel-admin/hostels/{hostelId}/complaints/{complaintId}"
    )
    @PreAuthorize("hasAnyRole('HOSTEL_ADMIN', 'WARDEN')")
    public ComplaintResponse updateStatus(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long complaintId,
            @Valid @RequestBody UpdateComplaintRequest request
    ) {
        return service.updateStatus(
                authentication.getName(),
                hostelId,
                complaintId,
                request
        );
    }
}
