package com.hostelhub.notification.dto;

import java.time.OffsetDateTime;

public record NotificationResponse(
        Long id,
        String notificationType,
        String title,
        String message,
        String targetUrl,
        boolean read,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {}
