
package com.hostelhub.room.service;

import com.hostelhub.block.repository.BlockRepository;
import com.hostelhub.flat.entity.Flat;
import com.hostelhub.flat.repository.FlatRepository;
import com.hostelhub.floor.entity.Floor;
import com.hostelhub.floor.repository.FloorRepository;
import com.hostelhub.room.dto.CreateRoomRequest;
import com.hostelhub.room.dto.RoomResponse;
import com.hostelhub.room.dto.UpdateRoomRequest;
import com.hostelhub.room.entity.Room;
import com.hostelhub.room.entity.RoomPricingCategory;
import com.hostelhub.room.entity.RoomStatus;
import com.hostelhub.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;
    private final FlatRepository flatRepository;
    private final BlockRepository blockRepository;

    public RoomService(
            RoomRepository roomRepository,
            FloorRepository floorRepository,
            FlatRepository flatRepository,
            BlockRepository blockRepository
    ) {
        this.roomRepository = roomRepository;
        this.floorRepository = floorRepository;
        this.flatRepository = flatRepository;
        this.blockRepository = blockRepository;
    }

    // ---------- Direct rooms under a floor ----------

    @Transactional
    public RoomResponse createDirectRoom(
            Long hostelId, Long blockId, Long floorId,
            CreateRoomRequest request
    ) {
        Floor floor = validateFloor(hostelId, blockId, floorId);
        String number = normalizeRoomNumber(request.roomNumber());

        if (roomRepository.existsByFloor_IdAndRoomNumberIgnoreCase(
                floorId, number
        )) {
            throw new IllegalArgumentException(
                    "This room number already exists on the floor"
            );
        }

        Room room = new Room();
        room.setFloor(floor);
        room.setFlat(null);
        applyRequest(room, request, number);

        return saveRoom(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getDirectRooms(
            Long hostelId, Long blockId, Long floorId
    ) {
        validateFloor(hostelId, blockId, floorId);

        return roomRepository
                .findAllByFloor_IdOrderByRoomNumberAsc(floorId)
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getDirectRoom(
            Long hostelId, Long blockId, Long floorId, Long roomId
    ) {
        validateFloor(hostelId, blockId, floorId);
        return RoomResponse.from(findDirectRoom(floorId, roomId));
    }

    @Transactional
    public RoomResponse updateDirectRoom(
            Long hostelId, Long blockId, Long floorId,
            Long roomId, UpdateRoomRequest request
    ) {
        validateFloor(hostelId, blockId, floorId);
        Room room = findDirectRoom(floorId, roomId);
        String number = normalizeRoomNumber(request.roomNumber());

        if (roomRepository
                .existsByFloor_IdAndRoomNumberIgnoreCaseAndIdNot(
                        floorId, number, roomId
                )) {
            throw new IllegalArgumentException(
                    "This room number already exists on the floor"
            );
        }

        applyRequest(room, request, number);
        return saveRoom(room);
    }

    @Transactional
    public RoomResponse changeDirectRoomStatus(
            Long hostelId, Long blockId, Long floorId,
            Long roomId, RoomStatus status
    ) {
        validateFloor(hostelId, blockId, floorId);
        requireStatus(status);

        Room room = findDirectRoom(floorId, roomId);
        room.setStatus(status);

        return saveRoom(room);
    }

    // ---------- Rooms inside a flat ----------

    @Transactional
    public RoomResponse createFlatRoom(
            Long hostelId, Long blockId, Long floorId, Long flatId,
            CreateRoomRequest request
    ) {
        validateFlat(hostelId, blockId, floorId, flatId);
        String number = normalizeRoomNumber(request.roomNumber());

        if (roomRepository.existsByFlat_IdAndRoomNumberIgnoreCase(
                flatId, number
        )) {
            throw new IllegalArgumentException(
                    "This room number already exists in the flat"
            );
        }

        Flat flat = flatRepository.findByIdAndFloor_Id(flatId, floorId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Flat not found on this floor"));

        Room room = new Room();
        room.setFlat(flat);
        room.setFloor(null);
        applyRequest(room, request, number);

        return saveRoom(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getFlatRooms(
            Long hostelId, Long blockId, Long floorId, Long flatId
    ) {
        validateFlat(hostelId, blockId, floorId, flatId);

        return roomRepository
                .findAllByFlat_IdOrderByRoomNumberAsc(flatId)
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getFlatRoom(
            Long hostelId, Long blockId, Long floorId,
            Long flatId, Long roomId
    ) {
        validateFlat(hostelId, blockId, floorId, flatId);
        return RoomResponse.from(findFlatRoom(flatId, roomId));
    }

    @Transactional
    public RoomResponse updateFlatRoom(
            Long hostelId, Long blockId, Long floorId, Long flatId,
            Long roomId, UpdateRoomRequest request
    ) {
        validateFlat(hostelId, blockId, floorId, flatId);
        Room room = findFlatRoom(flatId, roomId);
        String number = normalizeRoomNumber(request.roomNumber());

        if (roomRepository
                .existsByFlat_IdAndRoomNumberIgnoreCaseAndIdNot(
                        flatId, number, roomId
                )) {
            throw new IllegalArgumentException(
                    "This room number already exists in the flat"
            );
        }

        applyRequest(room, request, number);
        return saveRoom(room);
    }

    @Transactional
    public RoomResponse changeFlatRoomStatus(
            Long hostelId, Long blockId, Long floorId, Long flatId,
            Long roomId, RoomStatus status
    ) {
        validateFlat(hostelId, blockId, floorId, flatId);
        requireStatus(status);

        Room room = findFlatRoom(flatId, roomId);
        room.setStatus(status);

        return saveRoom(room);
    }

    // ---------- Shared validation and helpers ----------

    private Floor validateFloor(
            Long hostelId, Long blockId, Long floorId
    ) {
        blockRepository.findByIdAndHostel_Id(blockId, hostelId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Block not found in the specified hostel"
                        ));

        return floorRepository.findByIdAndBlock_Id(floorId, blockId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Floor not found in the specified block"
                        ));
    }

    private void validateFlat(
            Long hostelId, Long blockId, Long floorId, Long flatId
    ) {
        validateFloor(hostelId, blockId, floorId);

        flatRepository.findByIdAndFloor_Id(flatId, floorId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Flat not found on the specified floor"
                        ));
    }

    private Room findDirectRoom(Long floorId, Long roomId) {
        return roomRepository.findByIdAndFloor_Id(roomId, floorId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Room not found directly under this floor"
                        ));
    }

    private Room findFlatRoom(Long flatId, Long roomId) {
        return roomRepository.findByIdAndFlat_Id(roomId, flatId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Room not found inside this flat"
                        ));
    }

    private void applyRequest(
            Room room,
            CreateRoomRequest request,
            String number
    ) {
        room.setRoomNumber(number);
        room.setName(normalizeOptional(request.name()));
        room.setRoomType(request.roomType());
        room.setPricingCategory(
                request.pricingCategory() == null
                        ? RoomPricingCategory.NORMAL
                        : request.pricingCategory()
        );
        room.setCapacity(request.capacity());
        room.setDescription(normalizeOptional(request.description()));
    }

    private void applyRequest(
            Room room,
            UpdateRoomRequest request,
            String number
    ) {
        room.setRoomNumber(number);
        room.setName(normalizeOptional(request.name()));
        room.setRoomType(request.roomType());
        room.setPricingCategory(
                request.pricingCategory() == null
                        ? RoomPricingCategory.NORMAL
                        : request.pricingCategory()
        );
        room.setCapacity(request.capacity());
        room.setDescription(normalizeOptional(request.description()));
    }

    private RoomResponse saveRoom(Room room) {
        try {
            return RoomResponse.from(roomRepository.saveAndFlush(room));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "Could not save room. Check the room number and parent."
            );
        }
    }

    private String normalizeRoomNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Room number is required");
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void requireStatus(RoomStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Room status is required");
        }
    }
}

