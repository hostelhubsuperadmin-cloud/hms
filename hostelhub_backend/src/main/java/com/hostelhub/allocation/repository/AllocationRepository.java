package com.hostelhub.allocation.repository;

import com.hostelhub.allocation.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AllocationRepository
        extends JpaRepository<Allocation, Long> {

    List<Allocation> findByHostelIdOrderByIdDesc(Long hostelId);

    List<Allocation> findByHostlerIdOrderByIdDesc(Long hostlerId);

    Optional<Allocation> findByIdAndHostelId(
            Long id,
            Long hostelId
    );

    boolean existsByBedIdAndStatusIn(
            Long bedId,
            List<Allocation.Status> statuses
    );

    boolean existsByHostlerIdAndStatusIn(
            Long hostlerId,
            List<Allocation.Status> statuses
    );
}
