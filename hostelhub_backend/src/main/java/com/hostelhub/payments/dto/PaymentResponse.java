package com.hostelhub.payments.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        Long hostelId,
        Long hostlerId,
        Long invoiceId,
        BigDecimal amount,
        String currency,
        String method,
        String status,
        String providerReference,
        String gatewayOrderId,
        String gatewayPaymentId,
        String manualReference,
        String proofOriginalFilename,
        OffsetDateTime proofUploadedAt,
        OffsetDateTime createdAt,
        OffsetDateTime verifiedAt,
        String reviewNote
) {}
