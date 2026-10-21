
package com.hostelhub.block.repository;

import com.hostelhub.block.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    List<Block> findAllByHostel_IdOrderByNameAsc(Long hostelId);

    Optional<Block> findByIdAndHostel_Id(Long id, Long hostelId);

    boolean existsByHostel_IdAndCodeIgnoreCase(
            Long hostelId,
            String code
    );

    boolean existsByHostel_IdAndCodeIgnoreCaseAndIdNot(
            Long hostelId,
            String code,
            Long id
    );
}

