package com.hostelhub.notification.service;

import com.hostelhub.notification.dto.CreateNotificationRequest;
import com.hostelhub.notification.dto.NotificationResponse;
import com.hostelhub.notification.entity.Notification;
import com.hostelhub.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification n = new Notification();
        n.setRecipientUserId(request.recipientUserId());
        n.setHostelId(request.hostelId());
        n.setNotificationType(request.notificationType());
        n.setTitle(request.title());
        n.setMessage(request.message());
        n.setTargetUrl(request.targetUrl());

        return toResponse(repository.save(n));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listMine(Long userId) {
        return repository
                .findByRecipientUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId, Long userId) {
        Notification n = repository
                .findByIdAndRecipientUserId(notificationId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Notification not found"));

        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(OffsetDateTime.now());
        }

        return toResponse(repository.save(n));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getNotificationType(), n.getTitle(),
                n.getMessage(), n.getTargetUrl(), n.isRead(),
                n.getCreatedAt(), n.getReadAt()
        );
    }
}