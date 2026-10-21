
package com.hostelhub.auth.service;

import com.hostelhub.auth.entity.PasswordResetOtp;
import com.hostelhub.auth.repository.PasswordResetOtpRepository;
import com.hostelhub.email.EmailService;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {

    private final PasswordResetOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset-otp-expiry-minutes:5}")
    private long otpExpiryMinutes;

    @Value("${app.password-reset-otp-max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp-pepper}")
    private String otpPepper;

    @Value("${app.password-reset-token-expiry-minutes:10}")
    private long resetTokenExpiryMinutes;

    public PasswordResetService(
            PasswordResetOtpRepository otpRepository,
            UserRepository userRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder
    ) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void requestOtp(String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);

        var optionalUser = userRepository.findByEmailIgnoreCase(normalized);

        // Do not disclose whether the email exists.
        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        if (!user.isActive()) {
            return;
        }

        // Invalidate prior unconsumed OTPs.
        otpRepository.findByUser_IdAndConsumedAtIsNull(user.getId())
                .forEach(old -> {
                    old.setConsumedAt(Instant.now());
                    otpRepository.save(old);
                });

        String otp = String.format(
                "%06d", secureRandom.nextInt(1_000_000)
        );

        PasswordResetOtp entity = new PasswordResetOtp();
        entity.setUser(user);
        entity.setOtpHash(hashOtp(otp));
        entity.setExpiresAt(
                Instant.now().plus(otpExpiryMinutes, ChronoUnit.MINUTES)
        );
        entity.setAttempts(0);
        entity.setMaxAttempts(maxAttempts);

        otpRepository.save(entity);

        emailService.sendText(
                user.getEmail(),
                "HostelHub password-reset OTP",
                "Hello " + user.getFullName() + ",\n\n"
                        + "Your password-reset OTP is: " + otp + "\n\n"
                        + "It expires in " + otpExpiryMinutes
                        + " minutes. Never share this code with anyone.\n\n"
                        + "If you did not request a password reset, ignore this email."
        );
    }

    @Transactional
    public String verifyOtp(String email, String otp) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid or expired OTP."
                ));

        PasswordResetOtp entity = otpRepository
                .findFirstByUser_IdAndConsumedAtIsNullOrderByCreatedAtDesc(
                        user.getId()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid or expired OTP."
                ));

        Instant now = Instant.now();

        if (!entity.getExpiresAt().isAfter(now)
                || entity.getAttempts() >= entity.getMaxAttempts()
                || entity.getVerifiedAt() != null) {
            throw new IllegalArgumentException(
                    "Invalid or expired OTP."
            );
        }

        entity.setAttempts(entity.getAttempts() + 1);

        if (!MessageDigest.isEqual(
                entity.getOtpHash().getBytes(StandardCharsets.UTF_8),
                hashOtp(otp).getBytes(StandardCharsets.UTF_8)
        )) {
            otpRepository.save(entity);
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String resetToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        entity.setVerifiedAt(now);
        entity.setResetTokenHash(sha256(resetToken));
        entity.setResetTokenExpiresAt(
                now.plus(resetTokenExpiryMinutes, ChronoUnit.MINUTES)
        );

        otpRepository.save(entity);

        return resetToken;
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        PasswordResetOtp entity = otpRepository
                .findByResetTokenHashAndConsumedAtIsNull(
                        sha256(resetToken)
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid or expired reset token."
                ));

        Instant now = Instant.now();

        if (entity.getVerifiedAt() == null
                || entity.getResetTokenExpiresAt() == null
                || !entity.getResetTokenExpiresAt().isAfter(now)
                || !entity.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException(
                    "Invalid or expired reset token."
            );
        }

        User user = entity.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        entity.setConsumedAt(now);
        otpRepository.save(entity);
    }

    private String hashOtp(String otp) {
        return sha256(otpPepper + ":" + otp);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to process security token.", ex
            );
        }
    }
}

