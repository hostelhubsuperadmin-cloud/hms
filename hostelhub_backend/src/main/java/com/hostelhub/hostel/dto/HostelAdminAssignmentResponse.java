
package com.hostelhub.hostel.dto;

import java.time.Instant;

public record HostelAdminAssignmentResponse(
        Long assignmentId,
        Long hostelId,
        String hostelName,
        Long adminId,
        String adminName,
        String adminEmail,
        Instant assignedAt
) {
}

