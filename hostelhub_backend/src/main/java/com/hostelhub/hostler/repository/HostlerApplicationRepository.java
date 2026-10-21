package com.hostelhub.hostler.repository;

import com.hostelhub.hostler.entity.HostlerApplication;
import com.hostelhub.hostler.entity.HostlerApplicationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostlerApplicationRepository
        extends JpaRepository<HostlerApplication, Long> {

    List<HostlerApplication> findAllByUser_IdOrderByCreatedAtDesc(
            Long userId);

    Optional<HostlerApplication> findByIdAndUser_Id(
            Long id, Long userId);

    boolean existsByUser_IdAndHostel_IdAndStatus(
            Long userId,
            Long hostelId,
            HostlerApplicationStatus status);

    List<HostlerApplication> findAllByHostel_IdOrderByCreatedAtDesc(
            Long hostelId);

    List<HostlerApplication> findAllByHostel_IdAndStatusOrderByCreatedAtDesc(
            Long hostelId,
            HostlerApplicationStatus status);

    List<HostlerApplication> findAllByStatusOrderByCreatedAtDesc(
            HostlerApplicationStatus status);
}

