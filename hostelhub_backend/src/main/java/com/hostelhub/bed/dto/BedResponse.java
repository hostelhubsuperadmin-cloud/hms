
package com.hostelhub.bed.dto;

import com.hostelhub.bed.entity.Bed;
import com.hostelhub.bed.entity.BedStatus;

import java.time.Instant;

public record BedResponse(
        Long id,
        Long hostelId,
        Long blockId,
        Long floorId,
        String floorName,
        Long flatId,
        String flatName,
        Long roomId,
        String roomNumber,
        String bedNumber,
        String name,
        String description,
        BedStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static BedResponse from(Bed bed) {
        var room = bed.getRoom();

        Long floorId;
        String floorName;
        Long flatId = null;
        String flatName = null;

        if (room.getFlat() != null) {
            var flat = room.getFlat();
            var floor = flat.getFloor();

            floorId = floor.getId();
            floorName = floor.getName();
            flatId = flat.getId();
            flatName = flat.getName();
        } else {
            var floor = room.getFloor();

            floorId = floor.getId();
            floorName = floor.getName();
        }

        var floor = room.getFlat() != null
                ? room.getFlat().getFloor()
                : room.getFloor();

        var block = floor.getBlock();
        var hostel = block.getHostel();

        return new BedResponse(
                bed.getId(),
                hostel.getId(),
                block.getId(),
                floorId,
                floorName,
                flatId,
                flatName,
                room.getId(),
                room.getRoomNumber(),
                bed.getBedNumber(),
                bed.getName(),
                bed.getDescription(),
                bed.getStatus(),
                bed.getCreatedAt(),
                bed.getUpdatedAt()
        );
    }
}

