package com.hostelhub.billing.service;

import com.hostelhub.allocation.entity.Allocation;
import com.hostelhub.allocation.repository.AllocationRepository;
import com.hostelhub.bed.entity.Bed;
import com.hostelhub.bed.repository.BedRepository;
import com.hostelhub.billing.dto.InvoiceGenerationRequest;
import com.hostelhub.billing.dto.InvoiceResponse;
import com.hostelhub.billing.entity.Invoice;
import com.hostelhub.billing.repository.InvoiceRepository;
import com.hostelhub.hostler.service.HostelAccessService;
import com.hostelhub.pricing.entity.PricingRule;
import com.hostelhub.pricing.service.PricingService;
import com.hostelhub.room.entity.Room;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository repository;
    private final BillingCalculationService calculationService;
    private final AllocationRepository allocationRepository;
    private final BedRepository bedRepository;
    private final PricingService pricingService;
    private final HostelAccessService accessService;

    public InvoiceService(
            InvoiceRepository repository,
            BillingCalculationService calculationService,
            AllocationRepository allocationRepository,
            BedRepository bedRepository,
            PricingService pricingService,
            HostelAccessService accessService
    ) {
        this.repository = repository;
        this.calculationService = calculationService;
        this.allocationRepository = allocationRepository;
        this.bedRepository = bedRepository;
        this.pricingService = pricingService;
        this.accessService = accessService;
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listForHostler(Long hostlerId) {
        return repository
                .findByHostlerIdOrderByPeriodStartDesc(hostlerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listForHostel(
            String authenticatedEmail,
            Long hostelId,
            String status
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        List<Invoice> invoices;

        if (status == null || status.isBlank()) {
            invoices = repository
                    .findByHostelIdOrderByPeriodStartDesc(hostelId);
        } else {
            Invoice.Status parsedStatus;

            try {
                parsedStatus = Invoice.Status.valueOf(
                        status.trim().toUpperCase(Locale.ROOT)
                );
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Unsupported invoice status."
                );
            }

            invoices = repository
                    .findByHostelIdAndStatusOrderByPeriodStartDesc(
                            hostelId,
                            parsedStatus
                    );
        }

        return invoices.stream()
                .map(this::toResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<Long> findEligibleAllocationIds(
            String authenticatedEmail,
            Long hostelId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException(
                    "Billing period start and end dates are required."
            );
        }

        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException(
                    "Billing period end cannot be before its start."
            );
        }

        return allocationRepository
                .findByHostelIdOrderByIdDesc(hostelId)
                .stream()
                .filter(allocation ->
                        allocation.getStatus() == Allocation.Status.ACTIVE
                                || allocation.getStatus() == Allocation.Status.COMPLETED
                )
                .filter(allocation ->
                        !periodStart.isBefore(allocation.getStartDate())
                )
                .filter(allocation ->
                        allocation.getEndDate() == null
                                || !periodEnd.isAfter(allocation.getEndDate())
                )
                .filter(allocation ->
                        !repository.existsByAllocationIdAndPeriodStartAndPeriodEnd(
                                allocation.getId(),
                                periodStart,
                                periodEnd
                        )
                )
                .map(Allocation::getId)
                .toList();
    }

    public InvoiceResponse generateInvoice(
            String authenticatedEmail,
            Long hostelId,
            InvoiceGenerationRequest request
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        LocalDate start = request.periodStart();
        LocalDate end = request.periodEnd();

        if (end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "Invoice period end cannot be before its start."
            );
        }

        if (request.dueDate().isBefore(start)) {
            throw new IllegalArgumentException(
                    "Invoice due date cannot be before the billing period starts."
            );
        }

        Allocation allocation = allocationRepository
                .findByIdAndHostelId(
                        request.allocationId(),
                        hostelId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Allocation not found in this hostel."
                        )
                );

        if (allocation.getStatus() != Allocation.Status.ACTIVE
                && allocation.getStatus() != Allocation.Status.COMPLETED) {
            throw new IllegalArgumentException(
                    "Invoices can only be generated for checked-in or completed stays."
            );
        }

        if (start.isBefore(allocation.getStartDate())) {
            throw new IllegalArgumentException(
                    "Invoice period starts before the hostler checked in."
            );
        }

        if (allocation.getEndDate() != null
                && end.isAfter(allocation.getEndDate())) {
            throw new IllegalArgumentException(
                    "Invoice period extends beyond the actual check-out date."
            );
        }

        if (repository.existsByAllocationIdAndPeriodStartAndPeriodEnd(
                allocation.getId(),
                start,
                end
        )) {
            throw new IllegalArgumentException(
                    "An invoice already exists for this allocation and period."
            );
        }

        Bed bed = bedRepository.findById(allocation.getBedId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "The allocation's bed could not be found."
                        )
                );

        Room room = bed.getRoom();

        if (room.getCapacity() == null
                || room.getCapacity() < 1
                || room.getCapacity() > 5) {
            throw new IllegalArgumentException(
                    "Billing requires a room capacity between 1 and 5 to match the configured pricing rules."
            );
        }

        PricingRule.RoomType pricingCategory =
                PricingRule.RoomType.valueOf(
                        room.getPricingCategory().name()
                );

        BigDecimal subtotal = calculateCharge(
                hostelId,
                pricingCategory,
                room.getCapacity(),
                start,
                end
        );

        Invoice invoice = new Invoice();
        invoice.setHostelId(hostelId);
        invoice.setHostlerId(allocation.getHostlerId());
        invoice.setAllocationId(allocation.getId());
        invoice.setPeriodStart(start);
        invoice.setPeriodEnd(end);
        invoice.setDueDate(request.dueDate());
        invoice.setSubtotal(subtotal);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setStatus(Invoice.Status.ISSUED);
        invoice.setIssuedAt(OffsetDateTime.now());

        return toResponse(repository.saveAndFlush(invoice));
    }

    private BigDecimal calculateCharge(
            Long hostelId,
            PricingRule.RoomType pricingCategory,
            int sharingCapacity,
            LocalDate start,
            LocalDate end
    ) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate cursor = start;

        while (!cursor.isAfter(end)) {
            YearMonth month = YearMonth.from(cursor);

            LocalDate segmentEnd = month.atEndOfMonth().isBefore(end)
                    ? month.atEndOfMonth()
                    : end;

            BigDecimal monthlyRate = pricingService.getMonthlyRate(
                    hostelId,
                    pricingCategory,
                    sharingCapacity,
                    cursor
            );

            int days = (int) ChronoUnit.DAYS.between(
                    cursor,
                    segmentEnd
            ) + 1;

            total = total.add(
                    calculationService.prorate(
                            monthlyRate,
                            days,
                            month
                    )
            );

            cursor = segmentEnd.plusDays(1);
        }

        return total;
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        BigDecimal balance = invoice.getSubtotal()
                .subtract(invoice.getAmountPaid())
                .max(BigDecimal.ZERO);

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getHostelId(),
                invoice.getHostlerId(),
                invoice.getAllocationId(),
                invoice.getPeriodStart(),
                invoice.getPeriodEnd(),
                invoice.getDueDate(),
                invoice.getSubtotal(),
                invoice.getAmountPaid(),
                balance,
                invoice.getCurrency(),
                invoice.getStatus().name()
        );
    }

}
