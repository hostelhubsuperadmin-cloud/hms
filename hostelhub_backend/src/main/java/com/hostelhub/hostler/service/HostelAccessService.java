package com.hostelhub.hostler.service;

import com.hostelhub.hosteladmin.repository.HostelAdminAssignmentRepository;
import com.hostelhub.staff.repository.HostelStaffRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class HostelAccessService {

    private final HostelAdminAssignmentRepository adminAssignments;
    private final HostelStaffRepository staffAssignments;

    public HostelAccessService(
            HostelAdminAssignmentRepository adminAssignments,
            HostelStaffRepository staffAssignments
    ) {
        this.adminAssignments = adminAssignments;
        this.staffAssignments = staffAssignments;
    }

    public void requireCanManageHostel(String email, Long hostelId) {
        boolean isAssignedAdmin =
                adminAssignments.existsByUser_EmailIgnoreCaseAndHostel_Id(
                        email,
                        hostelId
                );

        boolean isAssignedWarden =
                staffAssignments
                        .existsByUser_EmailIgnoreCaseAndStaffRoleAndHostel_Id(
                                email,
                                "WARDEN",
                                hostelId
                        );

        if (!isAssignedAdmin && !isAssignedWarden) {
            throw new AccessDeniedException(
                    "You are not assigned to manage this hostel."
            );
        }
    }
}
