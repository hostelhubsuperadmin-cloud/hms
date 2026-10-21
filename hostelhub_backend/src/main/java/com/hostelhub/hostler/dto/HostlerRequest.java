package com.hostelhub.hostler.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record HostlerRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 120)
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 254)
        String email,

        @Size(max = 30)
        String phone,

        @Past(message = "Date of birth must be in the past")
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

        @Size(max = 2000)
        String notes
) {
}
