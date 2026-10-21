package com.hostelhub.staff.repository;

import com.hostelhub.staff.entity.HostelStaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostelStaffRepository
        extends JpaRepository<HostelStaffAssignment, Long> {

    List<HostelStaffAssignment>
    findAllByHostel_IdOrderByAssignedAtDesc(Long hostelId);

    Optional<HostelStaffAssignment> findByIdAndHostel_Id(
            Long id,
            Long hostelId
    );

    boolean existsByHostel_IdAndUser_Id(
            Long hostelId,
            Long userId
    );

    boolean existsByUser_IdAndStaffRoleAndHostel_IdNot(
            Long userId,
            String staffRole,
            Long hostelId
    );

    boolean existsByUser_EmailIgnoreCaseAndStaffRoleAndHostel_Id(
            String email,
            String staffRole,
            Long hostelId
    );
    List<HostelStaffAssignment>
    findAllByUser_EmailIgnoreCaseOrderByAssignedAtDesc(
            String email
    );
}

