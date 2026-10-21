package com.hostelhub.notification.controller;

import com.hostelhub.notification.dto.NotificationResponse;
import com.hostelhub.notification.service.NotificationService;
import com.hostelhub.security.AuthenticatedUserId;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> mine(Authentication authentication) {
        Long userId = AuthenticatedUserId.from(authentication);
        return service.listMine(userId);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        Long userId = AuthenticatedUserId.from(authentication);
        return service.markRead(notificationId, userId);
    }
}
