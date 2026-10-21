
package com.hostelhub.pricing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "pricing_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_pricing_rules_version",
                        columnNames = {
                                "hostel_id",
                                "room_type",
                                "sharing_capacity",
                                "billing_basis",
                                "effective_from"
                        }
                )
        }
)
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hostel_id", nullable = false)
    private Long hostelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType;

    @Column(name = "sharing_capacity", nullable = false)
    private Short sharingCapacity;

    @Column(name = "billing_basis", nullable = false, length = 20)
    private String billingBasis = "MONTHLY";

    @Column(
            name = "rate_per_person",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal ratePerPerson;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public enum RoomType {
        NORMAL,
        AC
    }

    public Long getId() {
        return id;
    }

    public Long getHostelId() {
        return hostelId;
    }

    public void setHostelId(Long hostelId) {
        this.hostelId = hostelId;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Short getSharingCapacity() {
        return sharingCapacity;
    }

    public void setSharingCapacity(Short sharingCapacity) {
        this.sharingCapacity = sharingCapacity;
    }

    public String getBillingBasis() {
        return billingBasis;
    }

    public void setBillingBasis(String billingBasis) {
        this.billingBasis = billingBasis;
    }

    public BigDecimal getRatePerPerson() {
        return ratePerPerson;
    }

    public void setRatePerPerson(BigDecimal ratePerPerson) {
        this.ratePerPerson = ratePerPerson;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
