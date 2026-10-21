package com.hostelhub.room.dto;

import com.hostelhub.room.entity.RoomPricingCategory;
import com.hostelhub.room.entity.RoomType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank(message = "Room number is required")
        @Size(max = 50)
        String roomNumber,

        @Size(max = 100)
        String name,

        @NotNull(message = "Room type is required")
        RoomType roomType,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 500, message = "Capacity cannot exceed 500")
        Integer capacity,

        @Size(max = 1000)
        String description,

        RoomPricingCategory pricingCategory
) {
}