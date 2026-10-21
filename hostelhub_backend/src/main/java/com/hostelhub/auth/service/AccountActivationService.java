
package com.hostelhub.auth.service;

import com.hostelhub.auth.entity.AccountActivationToken;
import com.hostelhub.auth.repository.AccountActivationTokenRepository;
import com.hostelhub.email.EmailService;
import com.hostelhub.email.EmailTemplateService;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

@Service
public class AccountActivationService {

    private final AccountActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.activation.expiry-hours:24}")
    private long expiryHours;

    public AccountActivationService(
            AccountActivationTokenRepository tokenRepository,
            UserRepository userRepository,
            EmailService emailService,
            EmailTemplateService emailTemplateService,
            PasswordEncoder passwordEncoder
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a single-use activation token and emails the activation link.
     * Call this after the user and staff assignment have been saved.
     */
    @Transactional
    public void createAndSendActivationEmail(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "A saved user is required to create an activation token."
            );
        }

        if (expiryHours <= 0) {
            throw new IllegalStateException(
                    "Activation expiry must be greater than zero hours."
            );
        }

        // Invalidate previously issued activation tokens.
        tokenRepository.deleteByUser_Id(user.getId());

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiryHours, ChronoUnit.HOURS);

        AccountActivationToken activationToken =
                new AccountActivationToken();

        activationToken.setUser(user);
        activationToken.setTokenHash(sha256(rawToken));
        activationToken.setExpiresAt(expiresAt);

        tokenRepository.save(activationToken);

        String normalizedFrontendUrl =
                frontendUrl.replaceAll("/+$", "");

        String activationLink =
                normalizedFrontendUrl + "/activate?token=" + rawToken;

        Map<String, Object> variables = Map.of(
                "fullName",
                user.getFullName() == null ? "" : user.getFullName(),
                "activationLink",
                activationLink,
                "expiryHours",
                expiryHours
        );

        String html = emailTemplateService.render(
                "account-activation.html",
                variables
        );

        emailService.sendHtml(
                user.getEmail(),
                "Activate your HostelHub account",
                html
        );
    }

    /**
     * Activates the account and sets the password.
     */
    @Transactional
    public void activateAndSetPassword(
            String rawToken,
            String newPassword
    ) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Activation token is required."
            );
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "A new password is required."
            );
        }

        AccountActivationToken activationToken = findValidToken(rawToken);

        User user = activationToken.getUser();

        if (user == null) {
            throw new IllegalArgumentException(
                    "The account associated with this token was not found."
            );
        }

        String status = user.getStatus() == null
                ? ""
                : user.getStatus().toString();

        if ("SUSPENDED".equalsIgnoreCase(status)
                || "DISABLED".equalsIgnoreCase(status)) {
            throw new IllegalStateException(
                    "This account cannot be activated. Contact an administrator."
            );
        }

        // Encode the password using the same encoder used during login.
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        // Critical: transition the pending account to an enabled state.
        user.activateAccount();

        userRepository.save(user);

        // Consume the token after successful account update.
        activationToken.setUsedAt(Instant.now());
        tokenRepository.save(activationToken);
    }

    /**
     * Legacy activation-only method.
     * The normal set-password workflow should use
     * activateAndSetPassword().
     */
    @Transactional
    public void activate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Activation token is required."
            );
        }

        AccountActivationToken activationToken = findValidToken(rawToken);

        User user = activationToken.getUser();

        if (user == null) {
            throw new IllegalArgumentException(
                    "The account associated with this token was not found."
            );
        }

        String status = user.getStatus() == null
                ? ""
                : user.getStatus().toString();

        if ("SUSPENDED".equalsIgnoreCase(status)
                || "DISABLED".equalsIgnoreCase(status)) {
            throw new IllegalStateException(
                    "This account cannot be activated. Contact an administrator."
            );
        }

        user.activateAccount();
        userRepository.save(user);

        activationToken.setUsedAt(Instant.now());
        tokenRepository.save(activationToken);
    }

    /**
     * Finds a token by its hash and verifies that it is valid and unused.
     */
    private AccountActivationToken findValidToken(String rawToken) {
        AccountActivationToken activationToken =
                tokenRepository.findByTokenHash(sha256(rawToken))
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid or expired activation link."
                        ));

        validateToken(activationToken);

        return activationToken;
    }

    private void validateToken(
            AccountActivationToken activationToken
    ) {
        if (activationToken.getUsedAt() != null) {
            throw new IllegalArgumentException(
                    "This activation link has already been used."
            );
        }

        if (activationToken.getExpiresAt() == null
                || !activationToken.getExpiresAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException(
                    "This activation link has expired. Request a new link."
            );
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available.",
                    exception
            );
        }
    }
}
