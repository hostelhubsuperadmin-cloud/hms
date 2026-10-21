package com.hostelhub.hostel.controller;

import com.hostelhub.hostel.dto.ActiveHostelResponse;
import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.entity.HostelStatus;
import com.hostelhub.hostel.repository.HostelRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostels")
public class ActiveHostelController {

    private final HostelRepository hostelRepository;

    public ActiveHostelController(HostelRepository hostelRepository) {
        this.hostelRepository = hostelRepository;
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActiveHostelResponse>> getActiveHostels() {

        List<ActiveHostelResponse> hostels =
                hostelRepository
                        .findAllByStatusOrderByNameAsc(HostelStatus.ACTIVE)
                        .stream()
                        .map(ActiveHostelResponse::from)
                        .toList();

        return ResponseEntity.ok(hostels);
    }
}
