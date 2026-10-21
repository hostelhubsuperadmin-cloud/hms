package com.hostelhub.payments.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
                @Index(name = "idx_payment_hostler", columnList = "hostler_id"),
                @Index(name = "idx_payment_provider_ref", columnList = "provider_reference")
        }
)
public class Payment {

    public enum Method {
        MANUAL, ONLINE
    }

    public enum Status {
        PENDING,
        SUBMITTED,
        PROCESSING,
        SUCCESS,
        FAILED,
        REJECTED,
        REFUNDED,
        RECONCILIATION_REQUIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "hostel_id", nullable = false)
    private Long hostelId;

    @Column(name = "hostler_id", nullable = false)
    private Long hostlerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status = Status.PENDING;

    @Column(name = "provider_reference", unique = true, length = 150)
    private String providerReference;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "gateway_order_id", unique = true, length = 100)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", unique = true, length = 100)
    private String gatewayPaymentId;

    @Column(name = "manual_reference", length = 100)
    private String manualReference;

    @Column(name = "proof_file_key", length = 100)
    private String proofFileKey;

    @Column(name = "proof_original_filename", length = 255)
    private String proofOriginalFilename;

    @Column(name = "proof_content_type", length = 100)
    private String proofContentType;

    @Column(name = "proof_uploaded_at")
    private OffsetDateTime proofUploadedAt;

    @Column(name = "submitted_by")
    private Long submittedBy;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long value) { invoiceId = value; }
    public Long getHostelId() { return hostelId; }
    public void setHostelId(Long value) { hostelId = value; }
    public Long getHostlerId() { return hostlerId; }
    public void setHostlerId(Long value) { hostlerId = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getCurrency() { return currency; }
    public void setCurrency(String value) { currency = value; }
    public Method getMethod() { return method; }
    public void setMethod(Method value) { method = value; }
    public Status getStatus() { return status; }
    public void setStatus(Status value) { status = value; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String value) { providerReference = value; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String value) { idempotencyKey = value; }
    public String getGatewayOrderId() { return gatewayOrderId; }
    public void setGatewayOrderId(String value) { gatewayOrderId = value; }
    public String getGatewayPaymentId() { return gatewayPaymentId; }
    public void setGatewayPaymentId(String value) { gatewayPaymentId = value; }
    public String getManualReference() { return manualReference; }
    public void setManualReference(String value) { manualReference = value; }
    public String getProofFileKey() { return proofFileKey; }
    public void setProofFileKey(String value) { proofFileKey = value; }
    public String getProofOriginalFilename() { return proofOriginalFilename; }
    public void setProofOriginalFilename(String value) { proofOriginalFilename = value; }
    public String getProofContentType() { return proofContentType; }
    public void setProofContentType(String value) { proofContentType = value; }
    public OffsetDateTime getProofUploadedAt() { return proofUploadedAt; }
    public void setProofUploadedAt(OffsetDateTime value) { proofUploadedAt = value; }
    public Long getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(Long value) { submittedBy = value; }
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long value) { reviewedBy = value; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String value) { reviewNote = value; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(OffsetDateTime value) { verifiedAt = value; }
}