package com.hostelhub.allocation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CheckOutRequest(
        @NotNull LocalDate checkOutDate
) {
}
