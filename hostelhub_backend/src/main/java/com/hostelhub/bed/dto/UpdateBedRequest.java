
package com.hostelhub.bed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBedRequest(

        @NotBlank(message = "Bed number is required")
        @Size(max = 50, message = "Bed number cannot exceed 50 characters")
        String bedNumber,

        @Size(max = 100, message = "Bed name cannot exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description

) {
}

