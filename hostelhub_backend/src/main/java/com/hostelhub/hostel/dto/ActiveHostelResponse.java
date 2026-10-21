package com.hostelhub.hostel.dto;

import com.hostelhub.hostel.entity.Hostel;

public record ActiveHostelResponse(
        Long id,
        String name,
        String city,
        String state
) {
    public static ActiveHostelResponse from(Hostel hostel) {
        return new ActiveHostelResponse(
                hostel.getId(),
                hostel.getName(),
                hostel.getCity(),
                hostel.getState()
        );
    }
}
