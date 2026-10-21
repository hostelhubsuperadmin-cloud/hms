package com.hostelhub.payments.dto;

import java.math.BigDecimal;

public record RazorpayOrderResponse(
        Long paymentId,
        Long invoiceId,
        String razorpayKeyId,
        String razorpayOrderId,
        long amountPaise,
        BigDecimal amount,
        String currency,
        String name,
        String description,
        String paymentStatus,
        String gatewayFeePolicy
) {}
