package com.hostelhub.hostel.dto;

import com.hostelhub.hostel.entity.Hostel;

public record PublicHostelResponse(
        Long id,
        String name,
        String description,
        String addressLine,
        String city,
        String state,
        String country,
        String postalCode,
        String contactPhone,
        String contactEmail,
        long totalBeds,
        long availableBeds
) {
    public static PublicHostelResponse from(
            Hostel hostel,
            long totalBeds,
            long availableBeds
    ) {
        return new PublicHostelResponse(
                hostel.getId(),
                hostel.getName(),
                hostel.getDescription(),
                hostel.getAddressLine(),
                hostel.getCity(),
                hostel.getState(),
                hostel.getCountry(),
                hostel.getPostalCode(),
                hostel.getContactPhone(),
                hostel.getContactEmail(),
                totalBeds,
                availableBeds
        );
    }
}
