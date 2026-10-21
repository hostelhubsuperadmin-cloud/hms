package com.hostelhub.payments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyRazorpayPaymentRequest(
        @NotBlank @Size(max = 100) String razorpayOrderId,
        @NotBlank @Size(max = 100) String razorpayPaymentId,
        @NotBlank @Size(max = 256) String razorpaySignature
) {}
