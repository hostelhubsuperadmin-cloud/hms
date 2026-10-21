package com.hostelhub.allocation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CheckInRequest(
        @NotNull LocalDate checkInDate
) {
}
