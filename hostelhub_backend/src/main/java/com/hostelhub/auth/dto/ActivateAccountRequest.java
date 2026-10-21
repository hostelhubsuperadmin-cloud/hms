

package com.hostelhub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateAccountRequest(
        @NotBlank(message = "Activation token is required")
        String token,

        @NotBlank(message = "New password is required")
        @Size(
                min = 12,
                max = 72,
                message = "Password must be between 12 and 72 characters"
        )
        String newPassword,

        @NotBlank(message = "Please confirm your password")
        String confirmPassword
) {
}


