package com.hostelhub.payments.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.payments.entity.PaymentWebhookEvent;
import com.hostelhub.payments.repository.PaymentWebhookEventRepository;
import com.hostelhub.payments.service.PaymentSignatureVerifier;
import com.hostelhub.payments.service.RazorpayGateway;
import com.hostelhub.payments.service.RazorpayPaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/payments/razorpay")
public class PaymentWebhookController {

    private final PaymentSignatureVerifier signatureVerifier;
    private final RazorpayPaymentService razorpayService;
    private final RazorpayGateway gateway;
    private final PaymentWebhookEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public PaymentWebhookController(
            PaymentSignatureVerifier signatureVerifier,
            RazorpayPaymentService razorpayService,
            RazorpayGateway gateway,
            PaymentWebhookEventRepository eventRepository,
            ObjectMapper objectMapper
    ) {
        this.signatureVerifier = signatureVerifier;
        this.razorpayService = razorpayService;
        this.gateway = gateway;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> webhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false)
            String signature,
            @RequestHeader(value = "x-razorpay-event-id", required = false)
            String eventId
    ) {
        if (!signatureVerifier.verifyWebhook(
                rawBody, signature, gateway.getWebhookSecret())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid webhook signature.");
        }

        if (eventId == null || eventId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing webhook event ID.");
        }

        if (eventRepository.existsById(eventId)) {
            return ResponseEntity.ok("Event already received.");
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String eventType = root.path("event").asText("");

            PaymentWebhookEvent event =
                    new PaymentWebhookEvent(eventId, eventType);
            eventRepository.saveAndFlush(event);

            if ("payment.captured".equals(eventType)) {
                JsonNode entity = root.path("payload")
                        .path("payment").path("entity");

                String paymentId = entity.path("id").asText(null);
                String orderId = entity.path("order_id").asText(null);
                String currency = entity.path("currency").asText(null);
                long amount = entity.path("amount").asLong(-1);

                if (paymentId == null || orderId == null || amount <= 0) {
                    throw new IllegalArgumentException(
                            "Webhook payment data is incomplete."
                    );
                }

                razorpayService.handleCapturedWebhook(
                        orderId, paymentId, amount, currency
                );
            }

            event.setProcessedAt(OffsetDateTime.now());
            eventRepository.save(event);
            return ResponseEntity.ok("Webhook processed.");
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to process Razorpay webhook.", exception
            );
        }
    }
}