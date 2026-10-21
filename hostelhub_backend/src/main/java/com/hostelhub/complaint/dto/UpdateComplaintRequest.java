package com.hostelhub.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateComplaintRequest(
        @NotBlank String status,
        @Size(max = 2000) String adminNote
) {}
