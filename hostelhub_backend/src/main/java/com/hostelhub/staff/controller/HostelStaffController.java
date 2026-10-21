
package com.hostelhub.staff.controller;

import com.hostelhub.staff.dto.CreateHostelStaffRequest;
import com.hostelhub.staff.dto.HostelStaffResponse;
import com.hostelhub.staff.dto.UpdateHostelStaffRequest;
import com.hostelhub.staff.service.HostelStaffService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostel-admin/hostels/{hostelId}/staff")
@PreAuthorize("hasRole('HOSTEL_ADMIN')")
public class HostelStaffController {

    private final HostelStaffService staffService;

    public HostelStaffController(HostelStaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ResponseEntity<List<HostelStaffResponse>> getStaff(
            Authentication authentication,
            @PathVariable Long hostelId) {

        return ResponseEntity.ok(
                staffService.getStaff(
                        authentication.getName(),
                        hostelId
                )
        );
    }

    @PostMapping
    public ResponseEntity<HostelStaffResponse> createStaff(
            Authentication authentication,
            @PathVariable Long hostelId,
            @Valid @RequestBody CreateHostelStaffRequest request) {

        HostelStaffResponse response = staffService.createStaff(
                authentication.getName(),
                hostelId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{staffId}")
    public ResponseEntity<HostelStaffResponse> updateStaff(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long staffId,
            @Valid @RequestBody UpdateHostelStaffRequest request) {

        return ResponseEntity.ok(
                staffService.updateStaff(
                        authentication.getName(),
                        hostelId,
                        staffId,
                        request
                )
        );
    }

    @DeleteMapping("/{staffId}")
    public ResponseEntity<Void> deleteStaff(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long staffId) {

        staffService.deleteStaff(
                authentication.getName(),
                hostelId,
                staffId
        );

        return ResponseEntity.noContent().build();
    }
}

