
package com.hostelhub.floor.controller;

import com.hostelhub.floor.dto.CreateFloorRequest;
import com.hostelhub.floor.dto.FloorResponse;
import com.hostelhub.floor.dto.UpdateFloorRequest;
import com.hostelhub.floor.entity.FloorStatus;
import com.hostelhub.floor.service.FloorService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/hostels/{hostelId}/blocks/{blockId}/floors"
)
@PreAuthorize("hasAuthority('HOSTEL_STRUCTURE_MANAGE')")
public class FloorController {

    private final FloorService floorService;

    public FloorController(FloorService floorService) {
        this.floorService = floorService;
    }

    @PostMapping
    public FloorResponse create(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @Valid @RequestBody CreateFloorRequest request
    ) {
        return floorService.create(hostelId, blockId, request);
    }

    @GetMapping
    public List<FloorResponse> findAll(
            @PathVariable Long hostelId,
            @PathVariable Long blockId
    ) {
        return floorService.findAll(hostelId, blockId);
    }

    @GetMapping("/{floorId}")
    public FloorResponse findById(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId
    ) {
        return floorService.findById(hostelId, blockId, floorId);
    }

    @PutMapping("/{floorId}")
    public FloorResponse update(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @Valid @RequestBody UpdateFloorRequest request
    ) {
        return floorService.update(
                hostelId, blockId, floorId, request
        );
    }

    @PatchMapping("/{floorId}/status")
    public FloorResponse changeStatus(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @RequestParam FloorStatus status
    ) {
        return floorService.changeStatus(
                hostelId, blockId, floorId, status
        );
    }
}

