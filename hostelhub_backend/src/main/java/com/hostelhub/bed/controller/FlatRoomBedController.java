
package com.hostelhub.bed.controller;

import com.hostelhub.bed.dto.BedResponse;
import com.hostelhub.bed.dto.CreateBedRequest;
import com.hostelhub.bed.dto.UpdateBedRequest;
import com.hostelhub.bed.entity.BedStatus;
import com.hostelhub.bed.service.BedService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/hostels/{hostelId}/blocks/{blockId}"
                + "/floors/{floorId}/flats/{flatId}/rooms/{roomId}/beds"
)
@PreAuthorize("hasAuthority('HOSTEL_STRUCTURE_MANAGE')")
public class FlatRoomBedController {

    private final BedService bedService;

    public FlatRoomBedController(BedService bedService) {
        this.bedService = bedService;
    }

    @PostMapping
    public BedResponse create(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @PathVariable Long roomId,
            @Valid @RequestBody CreateBedRequest request
    ) {
        return bedService.createFlatRoomBed(
                hostelId, blockId, floorId, flatId, roomId, request
        );
    }

    @GetMapping
    public List<BedResponse> getAll(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @PathVariable Long roomId
    ) {
        return bedService.getFlatRoomBeds(
                hostelId, blockId, floorId, flatId, roomId
        );
    }

    @GetMapping("/{bedId}")
    public BedResponse getById(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @PathVariable Long roomId,
            @PathVariable Long bedId
    ) {
        return bedService.getFlatRoomBedById(
                hostelId, blockId, floorId, flatId, roomId, bedId
        );
    }

    @PutMapping("/{bedId}")
    public BedResponse update(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @PathVariable Long roomId,
            @PathVariable Long bedId,
            @Valid @RequestBody UpdateBedRequest request
    ) {
        return bedService.updateFlatRoomBed(
                hostelId, blockId, floorId, flatId,
                roomId, bedId, request
        );
    }

    @PatchMapping("/{bedId}/status")
    public BedResponse changeStatus(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @PathVariable Long roomId,
            @PathVariable Long bedId,
            @RequestParam BedStatus status
    ) {
        return bedService.changeFlatRoomBedStatus(
                hostelId, blockId, floorId, flatId,
                roomId, bedId, status
        );
    }
}

