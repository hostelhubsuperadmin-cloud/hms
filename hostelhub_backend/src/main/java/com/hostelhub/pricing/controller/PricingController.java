
package com.hostelhub.pricing.controller;

import com.hostelhub.pricing.dto.PricingResponse;
import com.hostelhub.pricing.entity.PricingRule;
import com.hostelhub.pricing.repository.PricingRuleRepository;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/hostels/{hostelId}/pricing")
public class PricingController {

    private final PricingRuleRepository repository;

    public PricingController(PricingRuleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<PricingResponse> getRules(
            @PathVariable @Positive Long hostelId) {

        return repository.findByHostelIdAndActiveTrue(hostelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PricingResponse toResponse(PricingRule rule) {
        return new PricingResponse(
                rule.getId(),
                rule.getHostelId(),
                rule.getRoomType().name(),
                rule.getSharingCapacity(),
                rule.getBillingBasis(),
                rule.getRatePerPerson(),
                rule.getEffectiveFrom(),
                rule.getEffectiveTo(),
                rule.isActive()
        );
    }
}

