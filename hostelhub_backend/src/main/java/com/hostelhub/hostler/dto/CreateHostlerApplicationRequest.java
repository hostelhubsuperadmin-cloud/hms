
package com.hostelhub.hostler.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateHostlerApplicationRequest(

        @NotNull(message = "Hostel ID is required")
        Long hostelId,

        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,

        @Size(max = 30)
        String gender,

        @Size(max = 150)
        String guardianName,

        @Size(max = 20)
        String guardianPhone,

        @Size(max = 20)
        String emergencyContact,

        @Size(max = 255)
        String addressLine,

        @Size(max = 100)
        String city,

        @Size(max = 100)
        String state,

        @Size(max = 20)
        String postalCode,

        @FutureOrPresent(message = "Move-in date cannot be in the past")
        LocalDate requestedMoveInDate,

        @Size(max = 2000)
        String applicantNotes

) {
}

