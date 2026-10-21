package com.hostelhub.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceResponse(
        Long id,
        Long hostelId,
        Long hostlerId,
        Long allocationId,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal amountPaid,
        BigDecimal balance,
        String currency,
        String status
) {}
