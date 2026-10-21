package com.hostelhub.notification.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_recipient",
                        columnList = "recipient_user_id,created_at"),
                @Index(name = "idx_notification_unread",
                        columnList = "recipient_user_id,is_read")
        }
)
public class Notification {

    public enum DeliveryStatus {
        PENDING, SENT, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "hostel_id")
    private Long hostelId;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "target_url", length = 500)
    private String targetUrl;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 20)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(Long v) { recipientUserId = v; }
    public Long getHostelId() { return hostelId; }
    public void setHostelId(Long v) { hostelId = v; }
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String v) { notificationType = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { message = v; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String v) { targetUrl = v; }
    public boolean isRead() { return read; }
    public void setRead(boolean v) { read = v; }
    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus v) { deliveryStatus = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getReadAt() { return readAt; }
    public void setReadAt(OffsetDateTime v) { readAt = v; }
}
