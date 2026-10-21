
package com.hostelhub.room.controller;

import com.hostelhub.room.dto.CreateRoomRequest;
import com.hostelhub.room.dto.RoomResponse;
import com.hostelhub.room.dto.UpdateRoomRequest;
import com.hostelhub.room.entity.RoomStatus;
import com.hostelhub.room.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/hostels/{hostelId}/blocks/{blockId}/floors/{floorId}"
)
@PreAuthorize("hasAuthority('HOSTEL_STRUCTURE_MANAGE')")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // ===== Direct rooms under a floor =====

    @PostMapping("/rooms")
    public RoomResponse createDirectRoom(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        return roomService.createDirectRoom(
                hostelId, blockId, floorId, request
        );
    }

    @GetMapping("/rooms")
    public List<RoomResponse> getDirectRooms(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId
    ) {
        return roomService.getDirectRooms(hostelId, blockId, floorId);
    }

    @GetMapping("/rooms/{roomId}")
    public RoomResponse getDirectRoom(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long roomId
    ) {
        return roomService.getDirectRoom(
                hostelId, blockId, floorId, roomId
        );
    }

    @PutMapping("/rooms/{roomId}")
    public RoomResponse updateDirectRoom(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateRoomRequest request
    ) {
        return roomService.updateDirectRoom(
                hostelId, blockId, floorId, roomId, request
        );
    }

    @PatchMapping("/rooms/{roomId}/status")
    public RoomResponse changeDirectRoomStatus(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long roomId,
            @RequestParam RoomStatus status
    ) {
        return roomService.changeDirectRoomStatus(
                hostelId, blockId, floorId, roomId, status
        );
    }

    // ===== Rooms inside a flat =====

    @PostMapping("/flats/{flatId}/rooms")
    public RoomResponse createFlatRoom(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        return roomService.createFlatRoom(
                hostelId, blockId, floorId, flatId, request
        );
    }

    @GetMapping("/flats/{flatId}/rooms")
    public List<RoomResponse> getFlatRooms(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId
    ) {
        return roomService.getFlatRooms(
                hostelId, blockId, floorId, flatId
        );
    }

    @GetMapping("/flats/{flatId}/rooms/{roomId}")
    public RoomResponse getFlatRoom(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @PathVariable Long roomId
    ) {
        return roomService.getFlatRoom(
                hostelId, blockId, floorId, flatId, roomId
        );
    }

    @PutMapping("/flats/{flatId}/rooms/{roomId}")
    public RoomResponse updateFlatRoom(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateRoomRequest request
    ) {
        return roomService.updateFlatRoom(
                hostelId, blockId, floorId, flatId, roomId, request
        );
    }

    @PatchMapping("/flats/{flatId}/rooms/{roomId}/status")
    public RoomResponse changeFlatRoomStatus(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @PathVariable Long floorId,
            @PathVariable Long flatId,
            @PathVariable Long roomId,
            @RequestParam RoomStatus status
    ) {
        return roomService.changeFlatRoomStatus(
                hostelId, blockId, floorId, flatId, roomId, status
        );
    }
}

