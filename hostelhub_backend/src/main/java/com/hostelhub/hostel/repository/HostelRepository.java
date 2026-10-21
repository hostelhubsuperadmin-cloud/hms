package com.hostelhub.hostel.repository;

import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.entity.HostelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostelRepository extends JpaRepository<Hostel, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Hostel> findByCodeIgnoreCase(String code);

    List<Hostel> findAllByOrderByCreatedAtDesc();
    List<Hostel> findAllByStatusOrderByNameAsc(HostelStatus status);
}
