package com.hostelhub.audit.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "audit_events",
        indexes = {
                @Index(name = "idx_audit_actor", columnList = "actor_user_id"),
                @Index(name = "idx_audit_created", columnList = "created_at"),
                @Index(name = "idx_audit_entity",
                        columnList = "entity_type,entity_id")
        }
)
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "hostel_id")
    private Long hostelId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "outcome", nullable = false, length = 30)
    private String outcome;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long value) { actorUserId = value; }
    public Long getHostelId() { return hostelId; }
    public void setHostelId(Long value) { hostelId = value; }
    public String getAction() { return action; }
    public void setAction(String value) { action = value; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String value) { entityType = value; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String value) { entityId = value; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String value) { outcome = value; }
    public String getDetails() { return details; }
    public void setDetails(String value) { details = value; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String value) { correlationId = value; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
