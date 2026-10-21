
package com.hostelhub.user.entity;

import com.hostelhub.role.entity.Role;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    public static final String STATUS_PENDING_ACTIVATION =
            "PENDING_ACTIVATION";

    public static final String STATUS_ACTIVE = "ACTIVE";

    public static final String STATUS_SUSPENDED = "SUSPENDED";

    public static final String STATUS_DISABLED = "DISABLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 30)
    private String status = STATUS_ACTIVE;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {
        // Required by JPA.
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null || status.isBlank()) {
            status = STATUS_ACTIVE;
        }

        if (roles == null) {
            roles = new HashSet<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // ---------- Getters ----------

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    // ---------- Setters ----------

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        this.email = email.trim().toLowerCase(Locale.ROOT);
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash is required."
            );
        }

        this.passwordHash = passwordHash;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Account status is required."
            );
        }

        this.status = status.trim().toUpperCase(Locale.ROOT);
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    // ---------- Account lifecycle helpers ----------

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    public boolean isPendingActivation() {
        return STATUS_PENDING_ACTIVATION.equals(status);
    }

    public void markPendingActivation() {
        this.status = STATUS_PENDING_ACTIVATION;
        this.emailVerified = false;
    }

    public void activateAccount() {
        this.status = STATUS_ACTIVE;
        this.emailVerified = true;
    }

    public void markEmailVerified() {
        this.emailVerified = true;
    }

    // ---------- Role helpers ----------

    public void addRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }

        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }
}
