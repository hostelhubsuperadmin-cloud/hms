package com.hostelhub.complaint.dto;

import com.hostelhub.complaint.entity.Complaint;

import java.time.Instant;

public record ComplaintResponse(
        Long id,
        Long hostelId,
        String hostelName,
        Long hostlerId,
        String hostlerName,
        String hostlerEmail,
        String subject,
        String category,
        String priority,
        String description,
        String status,
        String adminNote,
        Instant createdAt,
        Instant updatedAt
) {
    public static ComplaintResponse from(Complaint c) {
        return new ComplaintResponse(
                c.getId(),
                c.getHostel().getId(),
                c.getHostel().getName(),
                c.getHostler().getId(),
                c.getHostler().getFullName(),
                c.getHostler().getEmail(),
                c.getSubject(),
                c.getCategory().name(),
                c.getPriority().name(),
                c.getDescription(),
                c.getStatus().name(),
                c.getAdminNote(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
