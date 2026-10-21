package com.hostelhub.staff.dto;

import com.hostelhub.staff.entity.HostelStaffAssignment;

import java.time.Instant;

public record HostelStaffResponse(
        Long assignmentId,
        Long userId,
        String fullName,
        String email,
        String phone,
        String role,
        String accountStatus,
        Long hostelId,
        String hostelName,
        Instant assignedAt
) {
    public static HostelStaffResponse from(
            HostelStaffAssignment assignment) {

        var user = assignment.getUser();
        var hostel = assignment.getHostel();

        return new HostelStaffResponse(
                assignment.getId(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                assignment.getStaffRole(),
                user.getStatus(),
                hostel.getId(),
                hostel.getName(),
                assignment.getAssignedAt()
        );
    }
}
