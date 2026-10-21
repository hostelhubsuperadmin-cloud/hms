package com.hostelhub.payments.service;

import com.hostelhub.billing.entity.Invoice;
import com.hostelhub.billing.repository.InvoiceRepository;
import com.hostelhub.hostler.service.HostelAccessService;
import com.hostelhub.payments.dto.CreatePaymentRequest;
import com.hostelhub.payments.dto.PaymentResponse;
import com.hostelhub.payments.entity.Payment;
import com.hostelhub.payments.repository.PaymentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final HostelAccessService accessService;

    public PaymentService(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            HostelAccessService accessService
    ) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.accessService = accessService;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listMine(Long hostlerId) {
        if (hostlerId == null) {
            throw new AccessDeniedException("Authenticated hostler required.");
        }
        return paymentRepository.findByHostlerIdOrderByCreatedAtDesc(hostlerId)
                .stream().map(this::toResponse).toList();
    }

    public PaymentResponse submitManualPayment(
            Long hostlerId,
            CreatePaymentRequest request
    ) {
        if (hostlerId == null) {
            throw new AccessDeniedException("Authenticated hostler required.");
        }
        if (request == null || request.invoiceId() == null
                || request.amount() == null
                || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("Valid payment details are required.");
        }

        String key = normalizeKey(request.idempotencyKey());
        if (key != null) {
            var existing = paymentRepository.findByIdempotencyKey(key);
            if (existing.isPresent()) {
                Payment old = existing.get();
                boolean same = Objects.equals(old.getHostlerId(), hostlerId)
                        && Objects.equals(old.getInvoiceId(), request.invoiceId())
                        && old.getAmount().compareTo(request.amount()) == 0
                        && old.getMethod() == Payment.Method.MANUAL;
                if (!same) {
                    throw new IllegalArgumentException(
                            "Idempotency key was used for another payment."
                    );
                }
                return toResponse(old);
            }
        }

        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found."));

        if (!Objects.equals(invoice.getHostlerId(), hostlerId)) {
            throw new AccessDeniedException("This invoice does not belong to you.");
        }
        validateInvoice(invoice);

        BigDecimal outstanding = invoice.getSubtotal().subtract(safePaid(invoice));
        if (request.amount().compareTo(outstanding) > 0) {
            throw new IllegalArgumentException(
                    "Payment exceeds the outstanding balance."
            );
        }

        Payment payment = new Payment();
        payment.setInvoiceId(invoice.getId());
        payment.setHostelId(invoice.getHostelId());
        payment.setHostlerId(hostlerId);
        payment.setAmount(request.amount());
        payment.setCurrency("INR");
        payment.setMethod(Payment.Method.MANUAL);
        payment.setStatus(Payment.Status.SUBMITTED);
        payment.setSubmittedBy(hostlerId);
        payment.setManualReference(normalizeReference(request.manualReference()));
        payment.setIdempotencyKey(key);

        return toResponse(paymentRepository.saveAndFlush(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listForHostel(
            String authenticatedEmail,
            Long hostelId,
            String status
    ) {
        accessService.requireCanManageHostel(authenticatedEmail, hostelId);

        List<Payment> rows;
        if (status == null || status.isBlank()) {
            rows = paymentRepository.findByHostelIdOrderByCreatedAtDesc(hostelId);
        } else {
            Payment.Status parsed;
            try {
                parsed = Payment.Status.valueOf(status.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported payment status.");
            }
            rows = paymentRepository
                    .findByHostelIdAndStatusOrderByCreatedAtDesc(hostelId, parsed);
        }
        return rows.stream().map(this::toResponse).toList();
    }

    public PaymentResponse reviewManualPayment(
            String authenticatedEmail,
            Long hostelId,
            Long paymentId,
            Long reviewerId,
            boolean approve,
            String note
    ) {
        accessService.requireCanManageHostel(authenticatedEmail, hostelId);

        Payment payment = paymentRepository.findByIdAndHostelIdForUpdate(
                paymentId, hostelId
        ).orElseThrow(() -> new IllegalArgumentException("Payment not found."));

        if (payment.getMethod() != Payment.Method.MANUAL
                || payment.getStatus() != Payment.Status.SUBMITTED) {
            throw new IllegalArgumentException(
                    "Only submitted manual payments can be reviewed."
            );
        }

        if (approve) {
            Invoice invoice = invoiceRepository.findByIdAndHostelIdForUpdate(
                    payment.getInvoiceId(), hostelId
            ).orElseThrow(() -> new IllegalArgumentException("Invoice not found."));

            if (!Objects.equals(invoice.getHostlerId(), payment.getHostlerId())) {
                throw new IllegalStateException("Payment and invoice hostler mismatch.");
            }
            validateInvoice(invoice);

            BigDecimal outstanding = invoice.getSubtotal().subtract(safePaid(invoice));
            if (payment.getAmount().compareTo(outstanding) > 0) {
                throw new IllegalArgumentException(
                        "The amount exceeds the current outstanding balance."
                );
            }

            invoice.setAmountPaid(safePaid(invoice).add(payment.getAmount()));
            updateInvoiceStatus(invoice);
            invoiceRepository.saveAndFlush(invoice);

            payment.setStatus(Payment.Status.SUCCESS);
            payment.setVerifiedAt(OffsetDateTime.now());
        } else {
            payment.setStatus(Payment.Status.REJECTED);
        }

        payment.setReviewedBy(reviewerId);
        payment.setReviewNote(note == null || note.isBlank()
                ? null : note.trim());

        return toResponse(paymentRepository.saveAndFlush(payment));
    }

    private void updateInvoiceStatus(Invoice invoice) {
        BigDecimal remaining = invoice.getSubtotal().subtract(safePaid(invoice));
        if (remaining.signum() <= 0) {
            invoice.setStatus(Invoice.Status.PAID);
        } else if (invoice.getDueDate() != null
                && invoice.getDueDate().isBefore(LocalDate.now())) {
            invoice.setStatus(Invoice.Status.OVERDUE);
        } else {
            invoice.setStatus(Invoice.Status.PARTIALLY_PAID);
        }
    }

    private void validateInvoice(Invoice invoice) {
        if (invoice.getStatus() != Invoice.Status.ISSUED
                && invoice.getStatus() != Invoice.Status.PARTIALLY_PAID
                && invoice.getStatus() != Invoice.Status.OVERDUE) {
            throw new IllegalArgumentException("Invoice is not accepting payments.");
        }
        if (invoice.getSubtotal() == null
                || invoice.getSubtotal().compareTo(safePaid(invoice)) <= 0) {
            throw new IllegalArgumentException("Invoice has no outstanding balance.");
        }
    }

    private BigDecimal safePaid(Invoice invoice) {
        return invoice.getAmountPaid() == null
                ? BigDecimal.ZERO : invoice.getAmountPaid();
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank()) return null;
        String value = key.trim();
        if (value.length() > 100) {
            throw new IllegalArgumentException("Idempotency key exceeds 100 characters.");
        }
        return value;
    }

    private String normalizeReference(String reference) {
        if (reference == null || reference.isBlank()) return null;
        String value = reference.trim();
        if (value.length() > 100) {
            throw new IllegalArgumentException("Manual reference exceeds 100 characters.");
        }
        return value;
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getHostelId(), p.getHostlerId(), p.getInvoiceId(),
                p.getAmount(), p.getCurrency(), p.getMethod().name(),
                p.getStatus().name(), p.getProviderReference(),
                p.getGatewayOrderId(), p.getGatewayPaymentId(),
                p.getManualReference(), p.getProofOriginalFilename(),
                p.getProofUploadedAt(), p.getCreatedAt(), p.getVerifiedAt(),
                p.getReviewNote()
        );
    }
}