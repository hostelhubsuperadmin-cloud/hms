
package com.hostelhub.bed.service;

import com.hostelhub.bed.dto.BedResponse;
import com.hostelhub.bed.dto.CreateBedRequest;
import com.hostelhub.bed.dto.UpdateBedRequest;
import com.hostelhub.bed.entity.Bed;
import com.hostelhub.bed.entity.BedStatus;
import com.hostelhub.bed.repository.BedRepository;
import com.hostelhub.block.repository.BlockRepository;
import com.hostelhub.floor.repository.FloorRepository;
import com.hostelhub.flat.entity.Flat;
import com.hostelhub.flat.repository.FlatRepository;
import com.hostelhub.room.entity.Room;
import com.hostelhub.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class BedService {

    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final BlockRepository blockRepository;
    private final FloorRepository floorRepository;
    private final FlatRepository flatRepository;

    public BedService(
            BedRepository bedRepository,
            RoomRepository roomRepository,
            BlockRepository blockRepository,
            FloorRepository floorRepository,
            FlatRepository flatRepository
    ) {
        this.bedRepository = bedRepository;
        this.roomRepository = roomRepository;
        this.blockRepository = blockRepository;
        this.floorRepository = floorRepository;
        this.flatRepository = flatRepository;
    }

    // =========================================================
    // Direct rooms under a floor
    // =========================================================

    @Transactional
    public BedResponse create(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long roomId,
            CreateBedRequest request
    ) {
        Room room = validateRoom(hostelId, blockId, floorId, roomId);

        if (room.getFlat() != null) {
            throw new IllegalArgumentException(
                    "This room belongs to a flat. Use the flat-room beds endpoint."
            );
        }

        return createForRoom(room, request);
    }

    @Transactional(readOnly = true)
    public List<BedResponse> getAll(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long roomId
    ) {
        Room room = validateRoom(hostelId, blockId, floorId, roomId);

        if (room.getFlat() != null) {
            throw new IllegalArgumentException(
                    "This room belongs to a flat. Use the flat-room beds endpoint."
            );
        }

        return getBedsForRoom(roomId);
    }

    @Transactional(readOnly = true)
    public BedResponse getById(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long roomId,
            Long bedId
    ) {
        Room room = validateRoom(hostelId, blockId, floorId, roomId);

        if (room.getFlat() != null) {
            throw new IllegalArgumentException(
                    "This room belongs to a flat. Use the flat-room beds endpoint."
            );
        }

        return BedResponse.from(findBed(roomId, bedId));
    }

    @Transactional
    public BedResponse update(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long roomId,
            Long bedId,
            UpdateBedRequest request
    ) {
        Room room = validateRoom(hostelId, blockId, floorId, roomId);

        if (room.getFlat() != null) {
            throw new IllegalArgumentException(
                    "This room belongs to a flat. Use the flat-room beds endpoint."
            );
        }

        return updateForRoom(roomId, bedId, request);
    }

    @Transactional
    public BedResponse changeStatus(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long roomId,
            Long bedId,
            BedStatus status
    ) {
        Room room = validateRoom(hostelId, blockId, floorId, roomId);

        if (room.getFlat() != null) {
            throw new IllegalArgumentException(
                    "This room belongs to a flat. Use the flat-room beds endpoint."
            );
        }

        return changeBedStatus(roomId, bedId, status);
    }

    // =========================================================
    // Rooms inside a flat
    // =========================================================

    @Transactional
    public BedResponse createFlatRoomBed(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long flatId,
            Long roomId,
            CreateBedRequest request
    ) {
        Room room = validateFlatRoom(
                hostelId, blockId, floorId, flatId, roomId
        );

        return createForRoom(room, request);
    }

    @Transactional(readOnly = true)
    public List<BedResponse> getFlatRoomBeds(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long flatId,
            Long roomId
    ) {
        validateFlatRoom(hostelId, blockId, floorId, flatId, roomId);

        return getBedsForRoom(roomId);
    }

    @Transactional(readOnly = true)
    public BedResponse getFlatRoomBedById(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long flatId,
            Long roomId,
            Long bedId
    ) {
        validateFlatRoom(hostelId, blockId, floorId, flatId, roomId);

        return BedResponse.from(findBed(roomId, bedId));
    }

    @Transactional
    public BedResponse updateFlatRoomBed(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long flatId,
            Long roomId,
            Long bedId,
            UpdateBedRequest request
    ) {
        validateFlatRoom(hostelId, blockId, floorId, flatId, roomId);

        return updateForRoom(roomId, bedId, request);
    }

    @Transactional
    public BedResponse changeFlatRoomBedStatus(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long flatId,
            Long roomId,
            Long bedId,
            BedStatus status
    ) {
        validateFlatRoom(hostelId, blockId, floorId, flatId, roomId);

        return changeBedStatus(roomId, bedId, status);
    }

    // =========================================================
    // Shared bed operations
    // =========================================================

    private BedResponse createForRoom(
            Room room,
            CreateBedRequest request
    ) {
        Long roomId = room.getId();
        String number = normalizeBedNumber(request.bedNumber());

        if (bedRepository.existsByRoom_IdAndBedNumberIgnoreCase(
                roomId, number
        )) {
            throw new IllegalArgumentException(
                    "This bed number already exists in the room"
            );
        }

        long currentBeds = bedRepository.countByRoom_Id(roomId);

        if (currentBeds >= room.getCapacity()) {
            throw new IllegalArgumentException(
                    "Cannot add more beds than the room capacity of "
                            + room.getCapacity()
            );
        }

        Bed bed = new Bed();
        bed.setRoom(room);
        bed.setBedNumber(number);
        bed.setName(normalizeOptional(request.name()));
        bed.setDescription(normalizeOptional(request.description()));
        bed.setStatus(BedStatus.AVAILABLE);

        return saveBed(bed);
    }

    private List<BedResponse> getBedsForRoom(Long roomId) {
        return bedRepository
                .findAllByRoom_IdOrderByBedNumberAsc(roomId)
                .stream()
                .map(BedResponse::from)
                .toList();
    }

    private BedResponse updateForRoom(
            Long roomId,
            Long bedId,
            UpdateBedRequest request
    ) {
        Bed bed = findBed(roomId, bedId);
        String number = normalizeBedNumber(request.bedNumber());

        if (bedRepository.existsByRoom_IdAndBedNumberIgnoreCaseAndIdNot(
                roomId, number, bedId
        )) {
            throw new IllegalArgumentException(
                    "Another bed with this number already exists in the room"
            );
        }

        bed.setBedNumber(number);
        bed.setName(normalizeOptional(request.name()));
        bed.setDescription(normalizeOptional(request.description()));

        return saveBed(bed);
    }

    private BedResponse changeBedStatus(
            Long roomId,
            Long bedId,
            BedStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException("Bed status is required");
        }

        if (status == BedStatus.OCCUPIED) {
            throw new IllegalArgumentException(
                    "Set OCCUPIED through the hostel allocation workflow"
            );
        }

        Bed bed = findBed(roomId, bedId);

        if (bed.getStatus() == BedStatus.OCCUPIED) {
            throw new IllegalArgumentException(
                    "An occupied bed cannot be manually changed. "
                            + "Complete or cancel its allocation first."
            );
        }

        bed.setStatus(status);
        return saveBed(bed);
    }

    // =========================================================
    // Validation
    // =========================================================

    private void validateHostelBlockAndFloor(
            Long hostelId,
            Long blockId,
            Long floorId
    ) {
        blockRepository.findByIdAndHostel_Id(blockId, hostelId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Block not found in the specified hostel"
                        )
                );

        floorRepository.findByIdAndBlock_Id(floorId, blockId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Floor not found in the specified block"
                        )
                );
    }

    private Room validateRoom(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long roomId
    ) {
        validateHostelBlockAndFloor(hostelId, blockId, floorId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Room not found")
                );

        Long actualFloorId = room.getFlat() != null
                ? room.getFlat().getFloor().getId()
                : room.getFloor().getId();

        if (!Objects.equals(actualFloorId, floorId)) {
            throw new EntityNotFoundException(
                    "Room does not belong to the specified floor"
            );
        }

        return room;
    }

    private Room validateFlatRoom(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long flatId,
            Long roomId
    ) {
        validateHostelBlockAndFloor(hostelId, blockId, floorId);

        Flat flat = flatRepository.findById(flatId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Flat not found")
                );

        if (flat.getFloor() == null
                || !Objects.equals(flat.getFloor().getId(), floorId)) {
            throw new EntityNotFoundException(
                    "Flat does not belong to the specified floor"
            );
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Room not found")
                );

        if (room.getFlat() == null
                || !Objects.equals(room.getFlat().getId(), flatId)) {
            throw new EntityNotFoundException(
                    "Room does not belong to the specified flat"
            );
        }

        return room;
    }

    private Bed findBed(Long roomId, Long bedId) {
        return bedRepository.findByIdAndRoom_Id(bedId, roomId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Bed not found in the specified room"
                        )
                );
    }

    private BedResponse saveBed(Bed bed) {
        try {
            return BedResponse.from(bedRepository.saveAndFlush(bed));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "Could not save bed. Check the bed number and room capacity."
            );
        }
    }

    private String normalizeBedNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Bed number is required");
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}


