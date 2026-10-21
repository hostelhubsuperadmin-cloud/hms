package com.hostelhub.hostler.controller;

import com.hostelhub.hostler.dto.HostlerRequest;
import com.hostelhub.hostler.dto.HostlerResponse;
import com.hostelhub.hostler.service.HostlerService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostels/{hostelId}/hostlers")
@PreAuthorize("hasAnyRole('HOSTEL_ADMIN', 'WARDEN')")
public class HostlerController {

    private final HostlerService hostlerService;

    public HostlerController(HostlerService hostlerService) {
        this.hostlerService = hostlerService;
    }

    @GetMapping
    public List<HostlerResponse> list(
            Authentication authentication,
            @PathVariable Long hostelId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status
    ) {
        return hostlerService.list(
                authentication.getName(),
                hostelId,
                q,
                status
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HostlerResponse create(
            Authentication authentication,
            @PathVariable Long hostelId,
            @Valid @RequestBody HostlerRequest request
    ) {
        return hostlerService.create(
                authentication.getName(),
                hostelId,
                request
        );
    }

    @PutMapping("/{hostlerId}")
    public HostlerResponse update(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long hostlerId,
            @Valid @RequestBody HostlerRequest request
    ) {
        return hostlerService.update(
                authentication.getName(),
                hostelId,
                hostlerId,
                request
        );
    }

    @DeleteMapping("/{hostlerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long hostlerId
    ) {
        hostlerService.deactivate(
                authentication.getName(),
                hostelId,
                hostlerId
        );
    }
}
