package com.hostelhub.hostler.dto;

import com.hostelhub.hostler.entity.HostlerApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewHostlerApplicationRequest(

        @NotNull(message = "Decision is required")
        HostlerApplicationStatus status,

        @Size(max = 2000, message = "Review notes cannot exceed 2000 characters")
        String reviewNotes

) {
}