
package com.hostelhub.hostel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHostelAdminRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name cannot exceed 120 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 255)
        String email,

        @Size(max = 30, message = "Phone cannot exceed 30 characters")
        String phone

) {
}
