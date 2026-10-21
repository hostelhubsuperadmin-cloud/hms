
package com.hostelhub.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PricingResponse(
        Long id,
        Long hostelId,
        String roomType,
        int sharingCapacity,
        String billingBasis,
        BigDecimal ratePerPerson,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active
) {
}

