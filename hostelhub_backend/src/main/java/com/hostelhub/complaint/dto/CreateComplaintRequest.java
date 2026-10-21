package com.hostelhub.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateComplaintRequest(
        @NotBlank
        @Size(max = 150)
        String subject,

        @NotNull
        String category,

        String priority,

        @NotBlank
        @Size(max = 3000)
        String description
) {}
