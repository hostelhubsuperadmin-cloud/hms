package com.hostelhub.staff.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateHostelStaffRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 120)
        String fullName,

        @NotBlank(message = "Email is required")
        @Email
        @Size(max = 254)
        String email,

        @Size(max = 30)
        String phone,

        @NotBlank(message = "Role is required")
        @Pattern(
                regexp = "WARDEN|STAFF",
                message = "Role must be WARDEN or STAFF"
        )
        String role

) {
}
