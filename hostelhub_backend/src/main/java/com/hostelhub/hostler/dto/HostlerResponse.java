package com.hostelhub.hostler.dto;

import com.hostelhub.hostler.entity.HostlerProfile;

import java.time.Instant;
import java.time.LocalDate;

public record HostlerResponse(
        Long hostlerId,
        Long hostelId,
        String hostelName,

        String fullName,
        String email,
        String phone,

        LocalDate dateOfBirth,
        String gender,
        String guardianName,
        String guardianPhone,
        String emergencyContact,

        String addressLine,
        String city,
        String state,
        String postalCode,
        String notes,

        String status,
        String accountStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static HostlerResponse from(HostlerProfile profile) {
        var user = profile.getUser();
        var hostel = profile.getHostel();

        return new HostlerResponse(
                user.getId(),
                hostel.getId(),
                hostel.getName(),

                user.getFullName(),
                user.getEmail(),
                user.getPhone(),

                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getGuardianName(),
                profile.getGuardianPhone(),
                profile.getEmergencyContact(),

                profile.getAddressLine(),
                profile.getCity(),
                profile.getState(),
                profile.getPostalCode(),
                profile.getNotes(),

                profile.getStatus(),
                user.getStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
