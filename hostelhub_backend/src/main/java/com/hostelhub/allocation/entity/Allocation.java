package com.hostelhub.allocation.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "allocations",
        indexes = {
                @Index(
                        name = "idx_allocation_hostler",
                        columnList = "hostler_id"
                ),
                @Index(
                        name = "idx_allocation_hostel_status",
                        columnList = "hostel_id,status"
                ),
                @Index(
                        name = "idx_allocation_bed_status",
                        columnList = "bed_id,status"
                )
        }
)
public class Allocation {

    public enum Status {
        RESERVED,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hostel_id", nullable = false)
    private Long hostelId;

    @Column(name = "hostler_id", nullable = false)
    private Long hostlerId;

    @Column(name = "bed_id", nullable = false)
    private Long bedId;

    @Column(name = "allocated_by", nullable = false)
    private Long allocatedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.RESERVED;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "planned_checkout_date")
    private LocalDate plannedCheckoutDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null) {
            status = Status.RESERVED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
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

    public Long getHostlerId() {
        return hostlerId;
    }

    public void setHostlerId(Long hostlerId) {
        this.hostlerId = hostlerId;
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public Long getAllocatedBy() {
        return allocatedBy;
    }

    public void setAllocatedBy(Long allocatedBy) {
        this.allocatedBy = allocatedBy;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getPlannedCheckoutDate() {
        return plannedCheckoutDate;
    }

    public void setPlannedCheckoutDate(LocalDate date) {
        this.plannedCheckoutDate = date;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
