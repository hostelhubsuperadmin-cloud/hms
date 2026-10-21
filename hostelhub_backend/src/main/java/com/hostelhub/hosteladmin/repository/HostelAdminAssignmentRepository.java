
package com.hostelhub.hosteladmin.repository;

import com.hostelhub.hosteladmin.entity.HostelAdminAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostelAdminAssignmentRepository
        extends JpaRepository<HostelAdminAssignment, Long> {

    Optional<HostelAdminAssignment> findByUser_EmailIgnoreCaseAndHostel_Id(
            String email,
            Long hostelId
    );

    boolean existsByUser_EmailIgnoreCaseAndHostel_Id(
            String email,
            Long hostelId
    );

    boolean existsByHostel_IdAndUser_Id(
            Long hostelId,
            Long userId
    );

    List<HostelAdminAssignment> findAllByUser_Id(Long userId);

    List<HostelAdminAssignment> findAllByHostel_Id(Long hostelId);
    long deleteAllByUser_Id(Long userId);

    boolean existsByAssignedBy_Id(Long userId);
    Optional<HostelAdminAssignment> findByUser_IdAndHostel_Id(
            Long userId,
            Long hostelId
    );
}

