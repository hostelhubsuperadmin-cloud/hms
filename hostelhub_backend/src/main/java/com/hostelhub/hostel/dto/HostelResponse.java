package com.hostelhub.hostel.dto;

import com.hostelhub.hostel.entity.Hostel;
import java.time.Instant;

public record HostelResponse(
        Long id,
        String name,
        String code,
        String description,
        String addressLine,
        String city,
        String state,
        String country,
        String postalCode,
        String contactPhone,
        String contactEmail,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static HostelResponse from(Hostel hostel) {
        return new HostelResponse(
                hostel.getId(),
                hostel.getName(),
                hostel.getCode(),
                hostel.getDescription(),
                hostel.getAddressLine(),
                hostel.getCity(),
                hostel.getState(),
                hostel.getCountry(),
                hostel.getPostalCode(),
                hostel.getContactPhone(),
                hostel.getContactEmail(),
                hostel.getStatus().name(),
                hostel.getCreatedAt(),
                hostel.getUpdatedAt()
        );
    }
}
