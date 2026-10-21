
package com.hostelhub.floor.dto;

import com.hostelhub.floor.entity.Floor;
import com.hostelhub.floor.entity.FloorStatus;

import java.time.Instant;

public record FloorResponse(
        Long id,
        Long hostelId,
        Long blockId,
        String blockName,
        String name,
        Integer floorNumber,
        String description,
        FloorStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static FloorResponse from(Floor floor) {
        return new FloorResponse(
                floor.getId(),
                floor.getBlock().getHostel().getId(),
                floor.getBlock().getId(),
                floor.getBlock().getName(),
                floor.getName(),
                floor.getFloorNumber(),
                floor.getDescription(),
                floor.getStatus(),
                floor.getCreatedAt(),
                floor.getUpdatedAt()
        );
    }
}

