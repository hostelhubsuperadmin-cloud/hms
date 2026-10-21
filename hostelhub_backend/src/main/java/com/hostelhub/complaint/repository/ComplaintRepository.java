package com.hostelhub.complaint.repository;

import com.hostelhub.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository
        extends JpaRepository<Complaint, Long> {

    List<Complaint> findAllByHostler_IdOrderByCreatedAtDesc(
            Long hostlerId
    );

    Optional<Complaint> findByIdAndHostler_Id(
            Long id,
            Long hostlerId
    );

    List<Complaint> findAllByHostel_IdOrderByCreatedAtDesc(
            Long hostelId
    );

    List<Complaint> findAllByHostel_IdAndStatusOrderByCreatedAtDesc(
            Long hostelId,
            Complaint.Status status
    );
    Optional<Complaint> findByIdAndHostel_Id(Long id, Long hostelId);
}
