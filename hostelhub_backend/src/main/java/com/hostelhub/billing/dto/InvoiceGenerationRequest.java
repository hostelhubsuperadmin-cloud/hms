package com.hostelhub.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record InvoiceGenerationRequest(
        @NotNull @Positive Long allocationId,
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd,
        @NotNull LocalDate dueDate
) {
}
