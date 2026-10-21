package com.hostelhub.payments.service;

import com.hostelhub.billing.entity.Invoice;
import com.hostelhub.billing.repository.InvoiceRepository;
import com.hostelhub.payments.dto.CreateRazorpayOrderRequest;
import com.hostelhub.payments.dto.RazorpayOrderResponse;
import com.hostelhub.payments.dto.RazorpayVerificationResponse;
import com.hostelhub.payments.dto.VerifyRazorpayPaymentRequest;
import com.hostelhub.payments.entity.Payment;
import com.hostelhub.payments.repository.PaymentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class RazorpayPaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayGateway gateway;
    private final PaymentSignatureVerifier signatureVerifier;

    public RazorpayPaymentService(
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            RazorpayGateway gateway,
            PaymentSignatureVerifier signatureVerifier
    ) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.gateway = gateway;
        this.signatureVerifier = signatureVerifier;
    }

    @Transactional
    public RazorpayOrderResponse createOrder(
            Long hostlerId,
            CreateRazorpayOrderRequest request
    ) {
        if (hostlerId == null) {
            throw new AccessDeniedException("Authenticated hostler required.");
        }

        if (request == null || request.amount() == null
                || request.invoiceId() == null) {
            throw new IllegalArgumentException("Payment details are required.");
        }

        String idempotencyKey = normalizeKey(request.idempotencyKey());

        if (idempotencyKey != null) {
            var existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                Payment old = existing.get();
                if (!Objects.equals(old.getHostlerId(), hostlerId)
                        || !Objects.equals(old.getInvoiceId(), request.invoiceId())
                        || old.getAmount().compareTo(request.amount()) != 0
                        || old.getMethod() != Payment.Method.ONLINE
                        || old.getGatewayOrderId() == null) {
                    throw new IllegalArgumentException(
                            "Idempotency key was already used for another request."
                    );
                }
                return toOrderResponse(old);
            }
        }

        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found."));

        if (!Objects.equals(invoice.getHostlerId(), hostlerId)) {
            throw new AccessDeniedException(
                    "You cannot pay another hostler's invoice."
            );
        }

        validateInvoiceAcceptsPayment(invoice);

        BigDecimal paid = safePaid(invoice);
        BigDecimal outstanding = invoice.getSubtotal().subtract(paid);

        if (request.amount().signum() <= 0
                || request.amount().compareTo(outstanding) > 0) {
            throw new IllegalArgumentException(
                    "Payment must be positive and cannot exceed the outstanding balance."
            );
        }

        if (!"INR".equalsIgnoreCase(invoice.getCurrency())) {
            throw new IllegalArgumentException(
                    "Razorpay checkout currently supports INR invoices only."
            );
        }

        Payment payment = new Payment();
        payment.setInvoiceId(invoice.getId());
        payment.setHostelId(invoice.getHostelId());
        payment.setHostlerId(hostlerId);
        payment.setAmount(request.amount());
        payment.setCurrency("INR");
        payment.setMethod(Payment.Method.ONLINE);
        payment.setStatus(Payment.Status.PROCESSING);
        payment.setSubmittedBy(hostlerId);
        payment.setIdempotencyKey(idempotencyKey);

        payment = paymentRepository.saveAndFlush(payment);

        /*
         * The order is created after a local payment record exists.
         * If the gateway call fails, the transaction rolls back locally.
         * Monitor gateway order creation failures in production.
         */
        String orderId = gateway.createOrder(
                request.amount(),
                "hhpay-" + payment.getId() + "-" + UUID.randomUUID(),
                invoice.getId(),
                payment.getId()
        );

        payment.setGatewayOrderId(orderId);
        paymentRepository.saveAndFlush(payment);

        return toOrderResponse(payment);
    }

    @Transactional
    public RazorpayVerificationResponse verifyCheckout(
            Long hostlerId,
            VerifyRazorpayPaymentRequest request
    ) {
        if (hostlerId == null) {
            throw new AccessDeniedException("Authenticated hostler required.");
        }

        Payment payment = paymentRepository
                .findByGatewayOrderIdForUpdate(request.razorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment order not found."
                ));

        if (!Objects.equals(payment.getHostlerId(), hostlerId)) {
            throw new AccessDeniedException("This payment does not belong to you.");
        }

        if (!signatureVerifier.verifyCheckout(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature(),
                gateway.getKeySecret()
        )) {
            throw new IllegalArgumentException("Invalid Razorpay signature.");
        }

        if (payment.getStatus() == Payment.Status.SUCCESS) {
            return verificationResponse(payment, "Payment already verified.");
        }

        RazorpayGateway.CapturedPayment providerPayment =
                gateway.fetchPayment(request.razorpayPaymentId());

        if (!Objects.equals(providerPayment.orderId(), payment.getGatewayOrderId())
                || !Objects.equals(providerPayment.paymentId(),
                request.razorpayPaymentId())
                || !"INR".equalsIgnoreCase(providerPayment.currency())
                || !"captured".equalsIgnoreCase(providerPayment.status())) {
            throw new IllegalArgumentException(
                    "Razorpay has not confirmed a captured payment for this order."
            );
        }

        long expectedPaise = payment.getAmount()
                .multiply(new BigDecimal("100"))
                .longValueExact();

        if (providerPayment.amountPaise() != expectedPaise) {
            throw new IllegalArgumentException(
                    "The captured amount does not match the payment amount."
            );
        }

        settleCapturedPayment(payment, providerPayment.paymentId());
        return verificationResponse(payment, "Payment verified successfully.");
    }

    @Transactional
    public void handleCapturedWebhook(
            String orderId,
            String paymentId,
            long amountPaise,
            String currency
    ) {
        Payment payment = paymentRepository.findByGatewayOrderIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment order not found."
                ));

        if (payment.getStatus() == Payment.Status.SUCCESS) {
            return;
        }

        long expectedPaise = payment.getAmount()
                .multiply(new BigDecimal("100"))
                .longValueExact();

        if (amountPaise != expectedPaise
                || !"INR".equalsIgnoreCase(currency)) {
            payment.setStatus(Payment.Status.RECONCILIATION_REQUIRED);
            paymentRepository.save(payment);
            return;
        }

        RazorpayGateway.CapturedPayment providerPayment =
                gateway.fetchPayment(paymentId);

        if (!Objects.equals(providerPayment.orderId(), orderId)
                || !"captured".equalsIgnoreCase(providerPayment.status())
                || providerPayment.amountPaise() != expectedPaise
                || !"INR".equalsIgnoreCase(providerPayment.currency())) {
            payment.setStatus(Payment.Status.RECONCILIATION_REQUIRED);
            paymentRepository.save(payment);
            return;
        }

        settleCapturedPayment(payment, paymentId);
    }

    private void settleCapturedPayment(Payment payment, String providerPaymentId) {
        Invoice invoice = invoiceRepository
                .findByIdAndHostelIdForUpdate(
                        payment.getInvoiceId(), payment.getHostelId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invoice not found for this payment."
                ));

        if (!Objects.equals(invoice.getHostlerId(), payment.getHostlerId())) {
            throw new IllegalStateException(
                    "Invoice and payment hostler do not match."
            );
        }

        if (payment.getStatus() == Payment.Status.SUCCESS) {
            return;
        }

        BigDecimal outstanding = invoice.getSubtotal().subtract(safePaid(invoice));

        /*
         * A captured payment that exceeds the current outstanding balance
         * must be reconciled; never silently over-credit the invoice.
         */
        if (outstanding.signum() <= 0
                || payment.getAmount().compareTo(outstanding) > 0) {
            payment.setStatus(Payment.Status.RECONCILIATION_REQUIRED);
            payment.setGatewayPaymentId(providerPaymentId);
            payment.setProviderReference(providerPaymentId);
            paymentRepository.saveAndFlush(payment);
            return;
        }

        invoice.setAmountPaid(safePaid(invoice).add(payment.getAmount()));

        BigDecimal remaining = invoice.getSubtotal().subtract(invoice.getAmountPaid());
        if (remaining.signum() == 0) {
            invoice.setStatus(Invoice.Status.PAID);
        } else if (invoice.getDueDate() != null
                && invoice.getDueDate().isBefore(LocalDate.now())) {
            invoice.setStatus(Invoice.Status.OVERDUE);
        } else {
            invoice.setStatus(Invoice.Status.PARTIALLY_PAID);
        }

        invoiceRepository.saveAndFlush(invoice);

        payment.setGatewayPaymentId(providerPaymentId);
        payment.setProviderReference(providerPaymentId);
        payment.setStatus(Payment.Status.SUCCESS);
        payment.setVerifiedAt(OffsetDateTime.now());
        paymentRepository.saveAndFlush(payment);
    }

    private void validateInvoiceAcceptsPayment(Invoice invoice) {
        if (invoice.getStatus() != Invoice.Status.ISSUED
                && invoice.getStatus() != Invoice.Status.PARTIALLY_PAID
                && invoice.getStatus() != Invoice.Status.OVERDUE) {
            throw new IllegalArgumentException(
                    "This invoice is not accepting payments."
            );
        }
    }

    private BigDecimal safePaid(Invoice invoice) {
        return invoice.getAmountPaid() == null
                ? BigDecimal.ZERO : invoice.getAmountPaid();
    }

    private RazorpayOrderResponse toOrderResponse(Payment payment) {
        return new RazorpayOrderResponse(
                payment.getId(),
                payment.getInvoiceId(),
                gateway.getKeyId(),
                payment.getGatewayOrderId(),
                payment.getAmount().multiply(new BigDecimal("100")).longValueExact(),
                payment.getAmount(),
                payment.getCurrency(),
                "HostelHub",
                "Invoice payment #" + payment.getInvoiceId(),
                payment.getStatus().name(),
                "HOSTEL_ABSORBS_GATEWAY_FEE"
        );
    }

    private RazorpayVerificationResponse verificationResponse(
            Payment payment, String message
    ) {
        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found."));

        BigDecimal paid = safePaid(invoice);
        return new RazorpayVerificationResponse(
                payment.getId(),
                invoice.getId(),
                payment.getStatus().name(),
                invoice.getSubtotal(),
                paid,
                invoice.getSubtotal().subtract(paid).max(BigDecimal.ZERO),
                message
        );
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank()) return null;
        String value = key.trim();
        if (value.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency key must not exceed 100 characters."
            );
        }
        return value;
    }
}
