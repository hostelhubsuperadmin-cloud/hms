package com.hostelhub.menu.repository;

import com.hostelhub.menu.entity.HostelMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostelMenuItemRepository
        extends JpaRepository<HostelMenuItem, Long> {

    List<HostelMenuItem>
    findAllByHostel_IdOrderByDayOfWeekAscMealTypeAscNameAsc(Long hostelId);

    List<HostelMenuItem>
    findAllByHostel_IdAndAvailableTrueOrderByDayOfWeekAscMealTypeAscNameAsc(
            Long hostelId
    );

    Optional<HostelMenuItem> findByIdAndHostel_Id(
            Long id,
            Long hostelId
    );
}

