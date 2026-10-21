
package com.hostelhub.floor.service;

import com.hostelhub.block.entity.Block;
import com.hostelhub.block.repository.BlockRepository;
import com.hostelhub.floor.dto.CreateFloorRequest;
import com.hostelhub.floor.dto.FloorResponse;
import com.hostelhub.floor.dto.UpdateFloorRequest;
import com.hostelhub.floor.entity.Floor;
import com.hostelhub.floor.entity.FloorStatus;
import com.hostelhub.floor.repository.FloorRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FloorService {

    private final FloorRepository floorRepository;
    private final BlockRepository blockRepository;

    public FloorService(
            FloorRepository floorRepository,
            BlockRepository blockRepository
    ) {
        this.floorRepository = floorRepository;
        this.blockRepository = blockRepository;
    }

    @Transactional
    public FloorResponse create(
            Long hostelId,
            Long blockId,
            CreateFloorRequest request
    ) {
        Block block = getBlock(hostelId, blockId);

        if (floorRepository.existsByBlock_IdAndFloorNumber(
                blockId, request.floorNumber()
        )) {
            throw new DataIntegrityViolationException(
                    "This floor number already exists in the block"
            );
        }

        Floor floor = new Floor();
        floor.setBlock(block);
        floor.setName(request.name().trim());
        floor.setFloorNumber(request.floorNumber());
        floor.setDescription(normalizeDescription(request.description()));
        floor.setStatus(FloorStatus.ACTIVE);

        return FloorResponse.from(floorRepository.save(floor));
    }

    @Transactional(readOnly = true)
    public List<FloorResponse> findAll(Long hostelId, Long blockId) {
        getBlock(hostelId, blockId);

        return floorRepository
                .findAllByBlock_IdOrderByFloorNumberAsc(blockId)
                .stream()
                .map(FloorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FloorResponse findById(
            Long hostelId,
            Long blockId,
            Long floorId
    ) {
        getBlock(hostelId, blockId);
        return FloorResponse.from(getFloor(blockId, floorId));
    }

    @Transactional
    public FloorResponse update(
            Long hostelId,
            Long blockId,
            Long floorId,
            UpdateFloorRequest request
    ) {
        getBlock(hostelId, blockId);
        Floor floor = getFloor(blockId, floorId);

        if (floorRepository.existsByBlock_IdAndFloorNumberAndIdNot(
                blockId, request.floorNumber(), floorId
        )) {
            throw new DataIntegrityViolationException(
                    "Another floor already uses this number in the block"
            );
        }

        floor.setName(request.name().trim());
        floor.setFloorNumber(request.floorNumber());
        floor.setDescription(normalizeDescription(request.description()));

        return FloorResponse.from(floorRepository.save(floor));
    }

    @Transactional
    public FloorResponse changeStatus(
            Long hostelId,
            Long blockId,
            Long floorId,
            FloorStatus status
    ) {
        getBlock(hostelId, blockId);
        Floor floor = getFloor(blockId, floorId);

        floor.setStatus(status);
        return FloorResponse.from(floorRepository.save(floor));
    }

    private Block getBlock(Long hostelId, Long blockId) {
        return blockRepository
                .findByIdAndHostel_Id(blockId, hostelId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Block " + blockId +
                                        " was not found in hostel " + hostelId
                        )
                );
    }

    private Floor getFloor(Long blockId, Long floorId) {
        return floorRepository.findByIdAndBlock_Id(floorId, blockId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Floor " + floorId +
                                        " was not found in block " + blockId
                        )
                );
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank()
                ? null
                : description.trim();
    }
}

