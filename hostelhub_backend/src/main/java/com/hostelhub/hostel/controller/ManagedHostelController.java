package com.hostelhub.hostel.controller;

import com.hostelhub.hostel.dto.ManagedHostelResponse;
import com.hostelhub.hostel.service.ManagedHostelService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/managed-hostels")
@PreAuthorize("hasAnyRole('HOSTEL_ADMIN', 'WARDEN')")
public class ManagedHostelController {

    private final ManagedHostelService service;

    public ManagedHostelController(ManagedHostelService service) {
        this.service = service;
    }

    @GetMapping
    public List<ManagedHostelResponse> list(
            Authentication authentication
    ) {
        return service.listForUser(authentication.getName());
    }
}
