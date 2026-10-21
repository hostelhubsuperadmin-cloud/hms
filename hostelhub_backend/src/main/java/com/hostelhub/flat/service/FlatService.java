
package com.hostelhub.flat.service;

import com.hostelhub.block.repository.BlockRepository;
import com.hostelhub.flat.dto.CreateFlatRequest;
import com.hostelhub.flat.dto.FlatResponse;
import com.hostelhub.flat.dto.UpdateFlatRequest;
import com.hostelhub.flat.entity.Flat;
import com.hostelhub.flat.entity.FlatStatus;
import com.hostelhub.flat.repository.FlatRepository;
import com.hostelhub.floor.repository.FloorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class FlatService {

    private final FlatRepository flatRepository;
    private final FloorRepository floorRepository;
    private final BlockRepository blockRepository;

    public FlatService(
            FlatRepository flatRepository,
            FloorRepository floorRepository,
            BlockRepository blockRepository
    ) {
        this.flatRepository = flatRepository;
        this.floorRepository = floorRepository;
        this.blockRepository = blockRepository;
    }

    @Transactional
    public FlatResponse create(
            Long hostelId,
            Long blockId,
            Long floorId,
            CreateFlatRequest request
    ) {
        validateHierarchy(hostelId, blockId, floorId);

        String code = normalizeCode(request.code());

        if (flatRepository.existsByFloor_IdAndCodeIgnoreCase(
                floorId, code
        )) {
            throw new IllegalArgumentException(
                    "A flat with this code already exists on this floor"
            );
        }

        Flat flat = new Flat();
        flat.setFloor(
                floorRepository.findByIdAndBlock_Id(floorId, blockId)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Floor not found"))
        );

        flat.setName(request.name().trim());
        flat.setCode(code);
        flat.setDescription(normalizeDescription(request.description()));
        flat.setStatus(FlatStatus.ACTIVE);

        try {
            return FlatResponse.from(flatRepository.saveAndFlush(flat));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "Could not create flat. Check for a duplicate flat code."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<FlatResponse> getAll(
            Long hostelId,
            Long blockId,
            Long floorId
    ) {
        validateHierarchy(hostelId, blockId, floorId);

        return flatRepository
                .findAllByFloor_IdOrderByCodeAsc(floorId)
                .stream()
                .map(FlatResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FlatResponse getById(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long flatId
    ) {
        validateHierarchy(hostelId, blockId, floorId);

        return FlatResponse.from(findFlat(floorId, flatId));
    }

    @Transactional
    public FlatResponse update(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long flatId,
            UpdateFlatRequest request
    ) {
        validateHierarchy(hostelId, blockId, floorId);

        Flat flat = findFlat(floorId, flatId);
        String code = normalizeCode(request.code());

        if (flatRepository.existsByFloor_IdAndCodeIgnoreCaseAndIdNot(
                floorId, code, flatId
        )) {
            throw new IllegalArgumentException(
                    "Another flat with this code already exists on this floor"
            );
        }

        flat.setName(request.name().trim());
        flat.setCode(code);
        flat.setDescription(normalizeDescription(request.description()));

        try {
            return FlatResponse.from(flatRepository.saveAndFlush(flat));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "Could not update flat. Check for a duplicate flat code."
            );
        }
    }

    @Transactional
    public FlatResponse changeStatus(
            Long hostelId,
            Long blockId,
            Long floorId,
            Long flatId,
            FlatStatus status
    ) {
        validateHierarchy(hostelId, blockId, floorId);

        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        Flat flat = findFlat(floorId, flatId);
        flat.setStatus(status);

        return FlatResponse.from(flatRepository.save(flat));
    }

    private Flat findFlat(Long floorId, Long flatId) {
        return flatRepository.findByIdAndFloor_Id(flatId, floorId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Flat not found on the specified floor"
                        )
                );
    }

    private void validateHierarchy(
            Long hostelId,
            Long blockId,
            Long floorId
    ) {
        if (hostelId == null || blockId == null || floorId == null) {
            throw new IllegalArgumentException(
                    "Hostel, block and floor IDs are required"
            );
        }

        // Ensure the block belongs to the specified hostel.
        blockRepository.findByIdAndHostel_Id(blockId, hostelId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Block not found in the specified hostel"
                        )
                );

        // Ensure the floor belongs to the specified block.
        floorRepository.findByIdAndBlock_Id(floorId, blockId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Floor not found in the specified block"
                        )
                );
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Flat code is required");
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

