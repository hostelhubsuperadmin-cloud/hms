package com.hostelhub.hostel.dto;

public record ManagedHostelResponse(
        Long hostelId,
        String hostelName,
        String role
) {
}
