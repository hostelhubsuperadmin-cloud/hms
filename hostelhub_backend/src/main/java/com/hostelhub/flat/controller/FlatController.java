
package com.hostelhub.flat.controller;

import com.hostelhub.flat.dto.CreateFlatRequest;
import com.hostelhub.flat.dto.FlatResponse;
import com.hostelhub.flat.dto.UpdateFlatRequest;
import com.hostelhub.flat.entity.FlatStatus;
import com.hostelhub.flat.service.FlatService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/hostels/{hostelId}/blocks/{blockId}/floors/{floorId}/flats"
)
@PreAuthorize("hasAuthority('HOSTEL_STRUCTURE_MANAGE')")
public class FlatController {

    private final FlatService flatService;

    public FlatController(FlatService flatService) {
        this.flatService = flatService;
    }

    @PostMapping
    public FlatResponse create(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @Valid @RequestBody CreateFlatRequest request
    ) {
        return flatService.create(hostelId, blockId, floorId, request);
    }

    @GetMapping
    public List<FlatResponse> getAll(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId
    ) {
        return flatService.getAll(hostelId, blockId, floorId);
    }

    @GetMapping("/{flatId}")
    public FlatResponse getById(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId
    ) {
        return flatService.getById(
                hostelId, blockId, floorId, flatId
        );
    }

    @PutMapping("/{flatId}")
    public FlatResponse update(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @Valid @RequestBody UpdateFlatRequest request
    ) {
        return flatService.update(
                hostelId, blockId, floorId, flatId, request
        );
    }

    @PatchMapping("/{flatId}/status")
    public FlatResponse changeStatus(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @RequestParam FlatStatus status
    ) {
        return flatService.changeStatus(
                hostelId, blockId, floorId, flatId, status
        );
    }
}

