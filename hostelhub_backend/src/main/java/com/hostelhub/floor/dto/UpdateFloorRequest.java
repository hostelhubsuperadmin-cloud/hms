
        package com.hostelhub.floor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFloorRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @Min(0)
        Integer floorNumber,

        @Size(max = 1000)
        String description
) {}

