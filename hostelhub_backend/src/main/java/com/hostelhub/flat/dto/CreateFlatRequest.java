
package com.hostelhub.flat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFlatRequest(

        @NotBlank(message = "Flat name is required")
        @Size(max = 100, message = "Flat name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "Flat code is required")
        @Size(max = 50, message = "Flat code cannot exceed 50 characters")
        String code,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description

) {
}

