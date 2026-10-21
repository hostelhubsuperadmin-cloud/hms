package com.hostelhub.hostler.controller;

import com.hostelhub.hostler.dto.CreateHostlerApplicationRequest;
import com.hostelhub.hostler.dto.HostlerApplicationResponse;
import com.hostelhub.hostler.service.HostlerApplicationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostler-applications")
public class HostlerApplicationController {

    private final HostlerApplicationService applicationService;

    public HostlerApplicationController(
            HostlerApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // POST: Submit a hostel application
    @PostMapping
    public ResponseEntity<HostlerApplicationResponse> createApplication(
            Authentication authentication,
            @Valid @RequestBody CreateHostlerApplicationRequest request) {

        HostlerApplicationResponse response =
                applicationService.createApplication(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET: Retrieve all applications belonging to the logged-in user
    @GetMapping("/my")
    public ResponseEntity<List<HostlerApplicationResponse>> getMyApplications(
            Authentication authentication) {

        List<HostlerApplicationResponse> applications =
                applicationService.getMyApplications(
                        authentication.getName()
                );

        return ResponseEntity.ok(applications);
    }

    // GET: Retrieve one application belonging to the logged-in user
    @GetMapping("/my/{applicationId}")
    public ResponseEntity<HostlerApplicationResponse> getMyApplication(
            Authentication authentication,
            @PathVariable Long applicationId) {

        HostlerApplicationResponse response =
                applicationService.getMyApplication(
                        authentication.getName(),
                        applicationId
                );

        return ResponseEntity.ok(response);
    }
}

