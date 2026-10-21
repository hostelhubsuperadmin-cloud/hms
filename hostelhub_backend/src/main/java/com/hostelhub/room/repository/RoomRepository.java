
package com.hostelhub.room.repository;

import com.hostelhub.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByFloor_IdOrderByRoomNumberAsc(Long floorId);

    List<Room> findAllByFlat_IdOrderByRoomNumberAsc(Long flatId);

    Optional<Room> findByIdAndFloor_Id(Long id, Long floorId);

    Optional<Room> findByIdAndFlat_Id(Long id, Long flatId);

    boolean existsByFloor_IdAndRoomNumberIgnoreCase(
            Long floorId, String roomNumber
    );

    boolean existsByFlat_IdAndRoomNumberIgnoreCase(
            Long flatId, String roomNumber
    );

    boolean existsByFloor_IdAndRoomNumberIgnoreCaseAndIdNot(
            Long floorId, String roomNumber, Long id
    );

    boolean existsByFlat_IdAndRoomNumberIgnoreCaseAndIdNot(
            Long flatId, String roomNumber, Long id
    );
}

