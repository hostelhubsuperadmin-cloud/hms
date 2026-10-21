package com.hostelhub.payments.dto;

import jakarta.validation.constraints.Size;

public record ReviewPaymentRequest(
        boolean approve,

        @Size(max = 500)
        String note
) {
}
