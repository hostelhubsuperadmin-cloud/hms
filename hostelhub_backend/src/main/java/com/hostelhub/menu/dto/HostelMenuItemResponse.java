package com.hostelhub.menu.dto;

import com.hostelhub.menu.entity.HostelMenuItem;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;

public record HostelMenuItemResponse(
        Long id,
        Long hostelId,
        DayOfWeek dayOfWeek,
        String mealType,
        String name,
        String description,
        BigDecimal price,
        boolean available,
        Instant createdAt,
        Instant updatedAt
) {
    public static HostelMenuItemResponse from(HostelMenuItem item) {
        return new HostelMenuItemResponse(
                item.getId(),
                item.getHostel().getId(),
                item.getDayOfWeek(),
                item.getMealType(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.isAvailable(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}

