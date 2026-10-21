package com.hostelhub.billing.controller;

import com.hostelhub.billing.dto.InvoiceGenerationRequest;
import com.hostelhub.billing.dto.InvoiceResponse;
import com.hostelhub.billing.service.InvoiceService;
import com.hostelhub.payments.dto.PaymentResponse;
import com.hostelhub.payments.dto.ReviewPaymentRequest;
import com.hostelhub.payments.service.PaymentService;
import com.hostelhub.security.AuthenticatedUserId;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import com.hostelhub.payments.service.PaymentProofService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
@RestController
@RequestMapping("/api/hostels/{hostelId}/billing")
@PreAuthorize("hasAnyRole('HOSTEL_ADMIN', 'WARDEN')")
public class HostelBillingController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final PaymentProofService paymentProofService;
    public HostelBillingController(
            InvoiceService invoiceService,
            PaymentService paymentService,PaymentProofService paymentProofService
    ) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.paymentProofService=paymentProofService;
    }
    @GetMapping("/invoices")
    public List<InvoiceResponse> invoices(
            Authentication authentication,
            @PathVariable Long hostelId,
            @RequestParam(required = false) String status
    ) {
        return invoiceService.listForHostel(
                authentication.getName(),
                hostelId,
                status
        );
    }

    @PostMapping("/invoices/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse generateInvoice(
            Authentication authentication,
            @PathVariable Long hostelId,
            @Valid @RequestBody InvoiceGenerationRequest request
    ) {
        return invoiceService.generateInvoice(
                authentication.getName(),
                hostelId,
                request
        );
    }

    @GetMapping("/payments")
    public List<PaymentResponse> payments(
            Authentication authentication,
            @PathVariable Long hostelId,
            @RequestParam(required = false) String status
    ) {
        return paymentService.listForHostel(
                authentication.getName(),
                hostelId,
                status
        );
    }

    @PatchMapping("/payments/{paymentId}/review")
    public PaymentResponse reviewPayment(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long paymentId,
            @Valid @RequestBody ReviewPaymentRequest request
    ) {
        Long reviewerId = AuthenticatedUserId.from(authentication);

        return paymentService.reviewManualPayment(
                authentication.getName(),
                hostelId,
                paymentId,
                reviewerId,
                request.approve(),
                request.note()
        );
    }
    @GetMapping("/payments/{paymentId}/proof")
    public ResponseEntity<byte[]> getPaymentProof(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long paymentId
    ) {
        PaymentProofService.ProofFile proof =
                paymentProofService.getForHostel(
                        authentication.getName(),
                        hostelId,
                        paymentId
                );

        MediaType contentType;

        try {
            contentType = MediaType.parseMediaType(proof.contentType());
        } catch (Exception ex) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"payment-proof\""
                )
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(proof.bytes());
    }
    @GetMapping("/invoices/eligible-allocations")
    public List<Long> eligibleAllocations(
            Authentication authentication,
            @PathVariable Long hostelId,
            @RequestParam LocalDate periodStart,
            @RequestParam LocalDate periodEnd
    ) {
        return invoiceService.findEligibleAllocationIds(
                authentication.getName(),
                hostelId,
                periodStart,
                periodEnd
        );
    }
}
