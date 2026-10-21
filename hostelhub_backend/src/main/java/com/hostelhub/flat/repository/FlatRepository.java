
package com.hostelhub.flat.repository;

import com.hostelhub.flat.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlatRepository extends JpaRepository<Flat, Long> {

    List<Flat> findAllByFloor_IdOrderByCodeAsc(Long floorId);

    Optional<Flat> findByIdAndFloor_Id(Long id, Long floorId);

    boolean existsByFloor_IdAndCodeIgnoreCase(
            Long floorId,
            String code
    );

    boolean existsByFloor_IdAndCodeIgnoreCaseAndIdNot(
            Long floorId,
            String code,
            Long id
    );
}

