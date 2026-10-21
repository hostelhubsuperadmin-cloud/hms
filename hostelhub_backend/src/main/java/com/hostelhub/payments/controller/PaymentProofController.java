package com.hostelhub.payments.controller;

import com.hostelhub.payments.dto.PaymentResponse;
import com.hostelhub.payments.entity.Payment;
import com.hostelhub.payments.service.PaymentProofService;
import com.hostelhub.payments.service.PaymentService;
import com.hostelhub.security.AuthenticatedHostlerId;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/payments")
public class PaymentProofController {

    private final com.hostelhub.payments.service.PaymentProofService proofService;



    public PaymentProofController(PaymentProofService proofService) {
        this.proofService = proofService;
    }

    @PostMapping(
            value = "/{paymentId}/proof",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.OK)
    public PaymentProofUploadResponse upload(
            Authentication auth,
            @PathVariable Long paymentId,
            @RequestPart("file") @NotNull MultipartFile file
    ) {
        Long hostlerId = AuthenticatedHostlerId.from(auth);
        Payment payment = proofService.upload(hostlerId, paymentId, file);

        return new PaymentProofUploadResponse(
                payment.getId(),
                payment.getProofOriginalFilename(),
                payment.getProofContentType(),
                payment.getProofUploadedAt()
        );
    }


    @GetMapping("/hostels/{hostelId}/billing/payments/{paymentId}/proof")
    public org.springframework.http.ResponseEntity<byte[]> viewPaymentProof(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long paymentId
    ) {
        var proof = proofService.getForHostel(
                authentication.getName(),
                hostelId,
                paymentId
        );

        org.springframework.http.MediaType mediaType =
                org.springframework.http.MediaType.parseMediaType(
                        proof.contentType()
                );

        if (!mediaType.equals(org.springframework.http.MediaType.IMAGE_JPEG)
                && !mediaType.equals(org.springframework.http.MediaType.IMAGE_PNG)
                && !mediaType.equals(org.springframework.http.MediaType.APPLICATION_PDF)) {
            throw new IllegalArgumentException(
                    "Unsupported payment proof content type."
            );
        }

        return org.springframework.http.ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"payment-proof\""
                )
                .header(
                        org.springframework.http.HttpHeaders.CACHE_CONTROL,
                        "no-store"
                )
                .body(proof.bytes());
    }

    public record PaymentProofUploadResponse(
            Long paymentId,
            String filename,
            String contentType,
            java.time.OffsetDateTime uploadedAt
    ) {}
}
