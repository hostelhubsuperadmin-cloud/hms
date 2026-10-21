
package com.hostelhub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InvitationRequest(
        @NotBlank @Email String email,
        @Size(max = 500) String message
) {}

