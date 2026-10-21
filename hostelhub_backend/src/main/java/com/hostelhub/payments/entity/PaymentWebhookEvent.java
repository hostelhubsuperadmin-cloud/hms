package com.hostelhub.payments.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_webhook_events")
public class PaymentWebhookEvent {

    @Id
    @Column(name = "event_id", length = 150)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt = OffsetDateTime.now();

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    protected PaymentWebhookEvent() {}

    public PaymentWebhookEvent(String eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.receivedAt = OffsetDateTime.now();
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(OffsetDateTime value) { processedAt = value; }
}
