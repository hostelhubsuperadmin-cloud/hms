package com.hostelhub.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull @Positive Long recipientUserId,
        Long hostelId,
        @NotBlank @Size(max = 50) String notificationType,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000) String message,
        @Size(max = 500) String targetUrl
) {}
