package com.hostelhub.payments.dto;

import java.math.BigDecimal;

public record RazorpayVerificationResponse(
        Long paymentId,
        Long invoiceId,
        String paymentStatus,
        BigDecimal invoiceTotal,
        BigDecimal amountPaid,
        BigDecimal outstandingBalance,
        String message
) {}
