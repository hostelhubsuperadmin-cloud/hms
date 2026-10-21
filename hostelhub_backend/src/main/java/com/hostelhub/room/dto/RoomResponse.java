package com.hostelhub.room.dto;

import com.hostelhub.room.entity.Room;
import com.hostelhub.room.entity.RoomPricingCategory;
import com.hostelhub.room.entity.RoomStatus;
import com.hostelhub.room.entity.RoomType;

import java.time.Instant;

public record RoomResponse(
        Long id,
        Long hostelId,
        Long blockId,
        Long floorId,
        String floorName,
        Long flatId,
        String flatName,
        String roomNumber,
        String name,
        RoomType roomType,
        RoomPricingCategory pricingCategory,
        Integer capacity,
        String description,
        RoomStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static RoomResponse from(Room room) {
        Long floorId;
        String floorName;
        Long flatId = null;
        String flatName = null;
        Long blockId;
        Long hostelId;

        if (room.getFlat() != null) {
            var flat = room.getFlat();
            var floor = flat.getFloor();

            floorId = floor.getId();
            floorName = floor.getName();
            flatId = flat.getId();
            flatName = flat.getName();
            blockId = floor.getBlock().getId();
            hostelId = floor.getBlock().getHostel().getId();
        } else {
            var floor = room.getFloor();

            floorId = floor.getId();
            floorName = floor.getName();
            blockId = floor.getBlock().getId();
            hostelId = floor.getBlock().getHostel().getId();
        }

        return new RoomResponse(
                room.getId(),
                hostelId,
                blockId,
                floorId,
                floorName,
                flatId,
                flatName,
                room.getRoomNumber(),
                room.getName(),
                room.getRoomType(),
                room.getPricingCategory(),
                room.getCapacity(),
                room.getDescription(),
                room.getStatus(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}