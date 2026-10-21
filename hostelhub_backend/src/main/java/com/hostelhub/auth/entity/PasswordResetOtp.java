

        package com.hostelhub.auth.entity;

import com.hostelhub.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "password_reset_otps")
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "otp_hash", nullable = false, length = 128)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 5;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "reset_token_hash", length = 128)
    private String resetTokenHash;

    @Column(name = "reset_token_expires_at")
    private Instant resetTokenExpiresAt;

    public PasswordResetOtp() {}

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String value) { this.otpHash = value; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant value) { this.expiresAt = value; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int value) { this.attempts = value; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int value) { this.maxAttempts = value; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant value) { this.verifiedAt = value; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant value) { this.consumedAt = value; }
    public Instant getCreatedAt() { return createdAt; }
    public String getResetTokenHash() { return resetTokenHash; }
    public void setResetTokenHash(String value) {
        this.resetTokenHash = value;
    }
    public Instant getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }
    public void setResetTokenExpiresAt(Instant value) {
        this.resetTokenExpiresAt = value;
    }
}
