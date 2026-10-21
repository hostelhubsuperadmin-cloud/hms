package com.hostelhub.allocation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateAllocationRequest(

        @NotNull
        @Positive
        Long hostlerId,

        @NotNull
        @Positive
        Long bedId,

        @NotNull
        LocalDate startDate,

        LocalDate plannedCheckoutDate
) {
}
