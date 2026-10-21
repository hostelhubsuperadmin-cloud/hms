
package com.hostelhub.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserStatusRequest(

        @NotBlank(message = "Status is required")
        @Pattern(
                regexp = "ACTIVE|SUSPENDED|DISABLED",
                message = "Status must be ACTIVE, SUSPENDED, or DISABLED"
        )
        String status

) {
}
