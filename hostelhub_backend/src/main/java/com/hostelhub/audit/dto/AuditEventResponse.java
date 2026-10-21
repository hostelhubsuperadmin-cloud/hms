package com.hostelhub.audit.dto;

import java.time.OffsetDateTime;

public record AuditEventResponse(
        Long id,
        Long actorUserId,
        Long hostelId,
        String action,
        String entityType,
        String entityId,
        String outcome,
        String details,
        String correlationId,
        OffsetDateTime createdAt
) {}
