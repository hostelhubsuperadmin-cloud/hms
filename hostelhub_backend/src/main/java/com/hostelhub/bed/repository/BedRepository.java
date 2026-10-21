package com.hostelhub.bed.repository;

import com.hostelhub.allocation.entity.Allocation;
import com.hostelhub.bed.entity.Bed;
import com.hostelhub.bed.entity.BedStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findAllByRoom_IdOrderByBedNumberAsc(Long roomId);

    Optional<Bed> findByIdAndRoom_Id(Long id, Long roomId);

    boolean existsByRoom_IdAndBedNumberIgnoreCase(
            Long roomId,
            String bedNumber
    );

    boolean existsByRoom_IdAndBedNumberIgnoreCaseAndIdNot(
            Long roomId,
            String bedNumber,
            Long id
    );

    long countByRoom_Id(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bed b WHERE b.id = :bedId")
    Optional<Bed> findByIdForUpdate(
            @Param("bedId") Long bedId
    );

    @Query("""
    SELECT b
    FROM Bed b
    JOIN b.room r
    LEFT JOIN r.floor directFloor
    LEFT JOIN directFloor.block directBlock
    LEFT JOIN r.flat flat
    LEFT JOIN flat.floor flatFloor
    LEFT JOIN flatFloor.block flatBlock
    WHERE b.status = :availableStatus
      AND (
        directBlock.hostel.id = :hostelId
        OR flatBlock.hostel.id = :hostelId
      )
      AND NOT EXISTS (
        SELECT a.id
        FROM Allocation a
        WHERE a.bedId = b.id
          AND a.status IN :blockedStatuses
      )
    ORDER BY b.bedNumber ASC
    """)
    List<Bed> findAvailableBedsForHostel(
            @Param("hostelId") Long hostelId,
            @Param("availableStatus") BedStatus availableStatus,
            @Param("blockedStatuses")
            List<Allocation.Status> blockedStatuses
    );
    // Count beds whose rooms are directly attached to a floor.
    long countByRoom_Floor_Block_Hostel_Id(Long hostelId);

    long countByRoom_Floor_Block_Hostel_IdAndStatus(
            Long hostelId,
            BedStatus status
    );

    // Count beds whose rooms are attached through a flat.
    long countByRoom_Flat_Floor_Block_Hostel_Id(Long hostelId);

    long countByRoom_Flat_Floor_Block_Hostel_IdAndStatus(
            Long hostelId,
            BedStatus status
    );
}
