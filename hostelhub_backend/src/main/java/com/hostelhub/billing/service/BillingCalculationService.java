package com.hostelhub.billing.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

@Service
public class BillingCalculationService {

    public BigDecimal prorate(
            BigDecimal monthlyRate,
            int billableDays,
            YearMonth month) {

        if (monthlyRate == null || monthlyRate.signum() < 0) {
            throw new IllegalArgumentException("Monthly rate cannot be negative");
        }

        if (billableDays < 0 || billableDays > month.lengthOfMonth()) {
            throw new IllegalArgumentException("Invalid billable day count");
        }

        return monthlyRate
                .multiply(BigDecimal.valueOf(billableDays))
                .divide(
                        BigDecimal.valueOf(month.lengthOfMonth()),
                        2,
                        RoundingMode.HALF_UP
                );
    }
}
