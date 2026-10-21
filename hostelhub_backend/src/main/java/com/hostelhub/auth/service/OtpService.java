
package com.hostelhub.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final String pepper;

    public OtpService(@Value("${app.otp-pepper}") String pepper) {
        if (pepper == null || pepper.length() < 32) {
            throw new IllegalArgumentException(
                    "app.otp-pepper must be a strong secret of at least 32 characters");
        }
        this.pepper = pepper;
    }

    public String generate() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    public String hash(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    pepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

            byte[] result = mac.doFinal(
                    value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(result);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash secret", ex);
        }
    }

    public boolean matches(String raw, String expectedHash) {
        byte[] actual = hash(raw).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }
}

