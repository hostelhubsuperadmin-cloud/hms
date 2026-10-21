package com.hostelhub.menu.controller;

import com.hostelhub.menu.dto.HostelMenuItemRequest;
import com.hostelhub.menu.dto.HostelMenuItemResponse;
import com.hostelhub.menu.service.HostelMenuService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HostelMenuController {

    private final HostelMenuService service;

    public HostelMenuController(HostelMenuService service) {
        this.service = service;
    }

    @GetMapping("/api/public/hostels/{hostelId}/menu")
    public List<HostelMenuItemResponse> getPublicMenu(
            @PathVariable Long hostelId
    ) {
        return service.getPublicMenu(hostelId);
    }

    @GetMapping("/api/hostel-admin/hostels/{hostelId}/menu")
    @PreAuthorize("hasRole('HOSTEL_ADMIN')")
    public List<HostelMenuItemResponse> getAdminMenu(
            Authentication auth,
            @PathVariable Long hostelId
    ) {
        return service.getAdminMenu(auth.getName(), hostelId);
    }

    @PostMapping("/api/hostel-admin/hostels/{hostelId}/menu")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HOSTEL_ADMIN')")
    public HostelMenuItemResponse create(
            Authentication auth,
            @PathVariable Long hostelId,
            @RequestBody HostelMenuItemRequest request
    ) {
        return service.create(auth.getName(), hostelId, request);
    }

    @PutMapping("/api/hostel-admin/hostels/{hostelId}/menu/{itemId}")
    @PreAuthorize("hasRole('HOSTEL_ADMIN')")
    public HostelMenuItemResponse update(
            Authentication auth,
            @PathVariable Long hostelId,
            @PathVariable Long itemId,
            @RequestBody HostelMenuItemRequest request
    ) {
        return service.update(
                auth.getName(), hostelId, itemId, request);
    }

    @DeleteMapping("/api/hostel-admin/hostels/{hostelId}/menu/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HOSTEL_ADMIN')")
    public void delete(
            Authentication auth,
            @PathVariable Long hostelId,
            @PathVariable Long itemId
    ) {
        service.delete(auth.getName(), hostelId, itemId);
    }
}

