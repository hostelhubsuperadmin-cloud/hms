
package com.hostelhub.pricing.service;

import com.hostelhub.pricing.entity.PricingRule;
import com.hostelhub.pricing.entity.PricingRule.RoomType;
import com.hostelhub.pricing.repository.PricingRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PricingService {

    private static final String MONTHLY = "MONTHLY";

    private final PricingRuleRepository repository;

    public PricingService(PricingRuleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthlyRate(
            Long hostelId,
            RoomType roomType,
            int sharingCapacity,
            LocalDate date) {

        if (hostelId == null || hostelId <= 0
                || roomType == null
                || sharingCapacity < 1
                || sharingCapacity > 5
                || date == null) {
            throw new IllegalArgumentException(
                    "Invalid hostel, room type, sharing capacity, or date"
            );
        }

        List<PricingRule> candidates =
                repository
                        .findByHostelIdAndRoomTypeAndSharingCapacityAndBillingBasisAndActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                                hostelId,
                                roomType,
                                (short) sharingCapacity,
                                MONTHLY,
                                date
                        );

        List<PricingRule> applicable = candidates.stream()
                .filter(rule ->
                        rule.getEffectiveTo() == null
                                || !rule.getEffectiveTo().isBefore(date))
                .toList();

        if (applicable.isEmpty()) {
            throw new IllegalStateException(
                    "No active pricing rule applies to the selected date"
            );
        }

        if (applicable.size() > 1) {
            throw new IllegalStateException(
                    "Overlapping pricing rules found for the selected date"
            );
        }

        return applicable.get(0).getRatePerPerson();
    }
}

