package com.hostelhub.hostler.controller;

import com.hostelhub.hostler.dto.HostlerApplicationResponse;
import com.hostelhub.hostler.dto.ReviewHostlerApplicationRequest;
import com.hostelhub.hostler.entity.HostlerApplicationStatus;
import com.hostelhub.hostler.service.HostlerApplicationReviewService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/hostler-applications")
@PreAuthorize("hasAuthority('HOSTLER_MANAGE')")
public class SuperAdminHostlerApplicationController {

    private final HostlerApplicationReviewService reviewService;

    public SuperAdminHostlerApplicationController(
            HostlerApplicationReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // GET: List all applications or filter by status.
    @GetMapping
    public ResponseEntity<List<HostlerApplicationResponse>> getApplications(
            @RequestParam(required = false)
            HostlerApplicationStatus status) {

        return ResponseEntity.ok(
                reviewService.getApplications(status));
    }

    // GET: View a particular application.
    @GetMapping("/{applicationId}")
    public ResponseEntity<HostlerApplicationResponse> getApplication(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(
                reviewService.getApplication(applicationId));
    }

    // PATCH: Approve or reject a pending application.
    @PatchMapping("/{applicationId}/review")
    public ResponseEntity<HostlerApplicationResponse> reviewApplication(
            @PathVariable Long applicationId,
            Authentication authentication,
            @Valid @RequestBody ReviewHostlerApplicationRequest request) {

        HostlerApplicationResponse response =
                reviewService.reviewApplication(
                        applicationId,
                        authentication.getName(),
                        request);

        return ResponseEntity.ok(response);
    }
}
