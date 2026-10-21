package com.hostelhub.billing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "invoices",
        indexes = {
                @Index(name = "idx_invoice_hostler", columnList = "hostler_id"),
                @Index(name = "idx_invoice_hostel_status",
                        columnList = "hostel_id,status")
        }
)
public class Invoice {

    public enum Status {
        DRAFT, ISSUED, PARTIALLY_PAID, PAID, OVERDUE, VOID
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hostel_id", nullable = false)
    private Long hostelId;

    @Column(name = "hostler_id", nullable = false)
    private Long hostlerId;

    @Column(name = "allocation_id", nullable = false)
    private Long allocationId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "issued_at")
    private OffsetDateTime issuedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Status status = Status.DRAFT;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getHostelId() { return hostelId; }
    public void setHostelId(Long v) { hostelId = v; }
    public Long getHostlerId() { return hostlerId; }
    public void setHostlerId(Long v) { hostlerId = v; }
    public Long getAllocationId() { return allocationId; }
    public void setAllocationId(Long v) { allocationId = v; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate v) { periodStart = v; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate v) { periodEnd = v; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate v) { dueDate = v; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal v) { subtotal = v; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal v) { amountPaid = v; }
    public String getCurrency() { return currency; }
    public Status getStatus() { return status; }
    public void setStatus(Status v) { status = v; }
    public OffsetDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(OffsetDateTime v) { issuedAt = v; }
}
