
package com.hostelhub.pricing.repository;

import com.hostelhub.pricing.entity.PricingRule;
import com.hostelhub.pricing.entity.PricingRule.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PricingRuleRepository
        extends JpaRepository<PricingRule, Long> {

    List<PricingRule> findByHostelIdAndActiveTrue(Long hostelId);

    List<PricingRule>
    findByHostelIdAndRoomTypeAndSharingCapacityAndBillingBasisAndActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            Long hostelId,
            RoomType roomType,
            Short sharingCapacity,
            String billingBasis,
            LocalDate date
    );
}

