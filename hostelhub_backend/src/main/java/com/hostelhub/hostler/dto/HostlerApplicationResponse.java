
package com.hostelhub.hostler.dto;

import com.hostelhub.hostler.entity.HostlerApplication;
import com.hostelhub.hostler.entity.HostlerApplicationStatus;

import java.time.Instant;
import java.time.LocalDate;

public record HostlerApplicationResponse(
        Long id,
        Long userId,
        String applicantName,
        String applicantEmail,
        Long hostelId,
        String hostelName,
        LocalDate dateOfBirth,
        String gender,
        String guardianName,
        String guardianPhone,
        String emergencyContact,
        String addressLine,
        String city,
        String state,
        String postalCode,
        LocalDate requestedMoveInDate,
        String applicantNotes,
        HostlerApplicationStatus status,
        Long reviewedById,
        Instant reviewedAt,
        String reviewNotes,
        Instant createdAt,
        Instant updatedAt
) {
    public static HostlerApplicationResponse from(
            HostlerApplication application
    ) {
        var user = application.getUser();
        var hostel = application.getHostel();

        return new HostlerApplicationResponse(
                application.getId(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                hostel.getId(),
                hostel.getName(),
                application.getDateOfBirth(),
                application.getGender(),
                application.getGuardianName(),
                application.getGuardianPhone(),
                application.getEmergencyContact(),
                application.getAddressLine(),
                application.getCity(),
                application.getState(),
                application.getPostalCode(),
                application.getRequestedMoveInDate(),
                application.getApplicantNotes(),
                application.getStatus(),
                application.getReviewedBy() == null
                        ? null : application.getReviewedBy().getId(),
                application.getReviewedAt(),
                application.getReviewNotes(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}

