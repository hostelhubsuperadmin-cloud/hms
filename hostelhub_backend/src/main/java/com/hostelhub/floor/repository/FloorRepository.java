
package com.hostelhub.floor.repository;

import com.hostelhub.floor.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FloorRepository extends JpaRepository<Floor, Long> {

    List<Floor> findAllByBlock_IdOrderByFloorNumberAsc(Long blockId);

    Optional<Floor> findByIdAndBlock_Id(Long id, Long blockId);

    boolean existsByBlock_IdAndFloorNumber(
            Long blockId,
            Integer floorNumber
    );

    boolean existsByBlock_IdAndFloorNumberAndIdNot(
            Long blockId,
            Integer floorNumber,
            Long id
    );
}

