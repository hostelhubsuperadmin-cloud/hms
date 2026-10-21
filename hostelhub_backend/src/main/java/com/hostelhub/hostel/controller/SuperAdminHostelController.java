package com.hostelhub.hostel.controller;

import com.hostelhub.hostel.dto.CreateHostelRequest;
import com.hostelhub.hostel.dto.HostelResponse;
import com.hostelhub.hostel.dto.UpdateHostelRequest;
import com.hostelhub.hostel.service.HostelService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/hostels")
@PreAuthorize("hasAuthority('HOSTEL_MANAGE')")
public class SuperAdminHostelController {

    private final HostelService hostelService;

    public SuperAdminHostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HostelResponse create(
            @Valid @RequestBody CreateHostelRequest request
    ) {
        return hostelService.create(request);
    }

    @GetMapping
    public List<HostelResponse> findAll() {
        return hostelService.findAll();
    }

    @GetMapping("/{id}")
    public HostelResponse findById(@PathVariable Long id) {
        return hostelService.findById(id);
    }

    @PutMapping("/{id}")
    public HostelResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHostelRequest request
    ) {
        return hostelService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public HostelResponse changeStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return hostelService.changeStatus(id, status);
    }
}
