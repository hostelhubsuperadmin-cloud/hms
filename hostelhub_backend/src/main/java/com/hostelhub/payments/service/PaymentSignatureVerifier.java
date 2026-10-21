package com.hostelhub.payments.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

@Component
public class PaymentSignatureVerifier {

    public boolean verifyCheckout(
            String orderId,
            String paymentId,
            String suppliedSignature,
            String secret
    ) {
        if (blank(orderId) || blank(paymentId)
                || blank(suppliedSignature) || blank(secret)) {
            return false;
        }

        String payload = orderId + "|" + paymentId;
        return constantTimeEquals(
                hmacSha256(payload, secret),
                suppliedSignature
        );
    }

    public boolean verifyWebhook(
            String rawBody,
            String suppliedSignature,
            String webhookSecret
    ) {
        if (rawBody == null || blank(suppliedSignature)
                || blank(webhookSecret)) {
            return false;
        }

        return constantTimeEquals(
                hmacSha256(rawBody, webhookSecret),
                suppliedSignature
        );
    }

    private String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            return HexFormat.of().formatHex(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to verify payment signature.", exception
            );
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        try {
            byte[] a = HexFormat.of().parseHex(expected);
            byte[] b = HexFormat.of().parseHex(actual);
            return MessageDigest.isEqual(a, b);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
