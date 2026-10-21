package com.hostelhub.hostler.repository;

import com.hostelhub.hostler.entity.HostlerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostlerProfileRepository
        extends JpaRepository<HostlerProfile, Long> {

    List<HostlerProfile> findAllByHostel_IdOrderByCreatedAtDesc(
            Long hostelId
    );

    Optional<HostlerProfile> findByIdAndHostel_Id(
            Long hostlerId,
            Long hostelId
    );

    Optional<HostlerProfile> findByUser_IdAndHostel_Id(
            Long userId,
            Long hostelId
    );

    Optional<HostlerProfile> findByUser_IdAndStatus(
            Long userId,
            String status
    );

    boolean existsByUser_IdAndHostel_Id(
            Long userId,
            Long hostelId
    );
}
