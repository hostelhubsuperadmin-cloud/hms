package com.hostelhub.payments.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RazorpayGateway {

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    @Value("${razorpay.webhook-secret:}")
    private String webhookSecret;

    public String getKeyId() {
        return keyId;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public String getKeySecret() {
        return keySecret;
    }

    public void requireConfigured() {
        if (blank(keyId) || blank(keySecret)) {
            throw new IllegalStateException(
                    "Razorpay credentials are not configured."
            );
        }
    }

    public String createOrder(
            BigDecimal amount,
            String receipt,
            Long invoiceId,
            Long paymentId
    ) {
        requireConfigured();

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            long paise = amount
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact();

            JSONObject request = new JSONObject();
            request.put("amount", paise);
            request.put("currency", "INR");
            request.put("receipt", receipt);
            request.put("partial_payment", false);

            JSONObject notes = new JSONObject();
            notes.put("invoice_id", invoiceId.toString());
            notes.put("hostelhub_payment_id", paymentId.toString());
            request.put("notes", notes);

            Order order = client.orders.create(request);
            return order.get("id");
        } catch (RazorpayException | ArithmeticException exception) {
            throw new IllegalStateException(
                    "Unable to create Razorpay order.", exception
            );
        }
    }

    public CapturedPayment fetchPayment(String paymentId) {
        requireConfigured();

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            com.razorpay.Payment payment =
                    client.payments.fetch(paymentId);

            return new CapturedPayment(
                    payment.get("id").toString(),
                    payment.get("order_id").toString(),
                    payment.get("currency").toString(),
                    payment.get("status").toString(),
                    Long.parseLong(payment.get("amount").toString())
            );
        } catch (RazorpayException exception) {
            throw new IllegalStateException(
                    "Unable to verify the payment with Razorpay.",
                    exception
            );
        }
    }

    public record CapturedPayment(
            String paymentId,
            String orderId,
            String currency,
            String status,
            long amountPaise
    ) {}

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
