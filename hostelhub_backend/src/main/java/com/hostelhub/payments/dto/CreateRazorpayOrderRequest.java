package com.hostelhub.payments.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateRazorpayOrderRequest(
        @NotNull @Positive Long invoiceId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Size(max = 100) String idempotencyKey
) {}
