
package com.hostelhub.flat.dto;

import com.hostelhub.flat.entity.Flat;
import com.hostelhub.flat.entity.FlatStatus;

import java.time.Instant;

public record FlatResponse(
        Long id,
        Long hostelId,
        Long blockId,
        Long floorId,
        String floorName,
        String name,
        String code,
        String description,
        FlatStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static FlatResponse from(Flat flat) {
        var floor = flat.getFloor();
        var block = floor.getBlock();
        var hostel = block.getHostel();

        return new FlatResponse(
                flat.getId(),
                hostel.getId(),
                block.getId(),
                floor.getId(),
                floor.getName(),
                flat.getName(),
                flat.getCode(),
                flat.getDescription(),
                flat.getStatus(),
                flat.getCreatedAt(),
                flat.getUpdatedAt()
        );
    }
}

