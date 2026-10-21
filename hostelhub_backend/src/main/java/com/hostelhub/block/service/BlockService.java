
package com.hostelhub.block.service;

import com.hostelhub.block.dto.BlockResponse;
import com.hostelhub.block.dto.CreateBlockRequest;
import com.hostelhub.block.dto.UpdateBlockRequest;
import com.hostelhub.block.entity.Block;
import com.hostelhub.block.entity.BlockStatus;
import com.hostelhub.block.repository.BlockRepository;
import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.repository.HostelRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final HostelRepository hostelRepository;

    public BlockService(
            BlockRepository blockRepository,
            HostelRepository hostelRepository
    ) {
        this.blockRepository = blockRepository;
        this.hostelRepository = hostelRepository;
    }

    @Transactional
    public BlockResponse create(
            Long hostelId,
            CreateBlockRequest request
    ) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Hostel not found: " + hostelId
                        )
                );

        String code = normalizeCode(request.code());

        if (blockRepository.existsByHostel_IdAndCodeIgnoreCase(
                hostelId, code
        )) {
            throw new DataIntegrityViolationException(
                    "A block with this code already exists in the hostel"
            );
        }

        Block block = new Block();
        block.setHostel(hostel);
        block.setName(request.name().trim());
        block.setCode(code);
        block.setDescription(normalizeDescription(request.description()));
        block.setStatus(BlockStatus.ACTIVE);

        return BlockResponse.from(blockRepository.save(block));
    }

    @Transactional(readOnly = true)
    public List<BlockResponse> findAll(Long hostelId) {
        if (!hostelRepository.existsById(hostelId)) {
            throw new EntityNotFoundException(
                    "Hostel not found: " + hostelId
            );
        }

        return blockRepository
                .findAllByHostel_IdOrderByNameAsc(hostelId)
                .stream()
                .map(BlockResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BlockResponse findById(Long hostelId, Long blockId) {
        return BlockResponse.from(getBlock(hostelId, blockId));
    }

    @Transactional
    public BlockResponse update(
            Long hostelId,
            Long blockId,
            UpdateBlockRequest request
    ) {
        Block block = getBlock(hostelId, blockId);
        String code = normalizeCode(request.code());

        if (blockRepository.existsByHostel_IdAndCodeIgnoreCaseAndIdNot(
                hostelId, code, blockId
        )) {
            throw new DataIntegrityViolationException(
                    "Another block already uses this code"
            );
        }

        block.setName(request.name().trim());
        block.setCode(code);
        block.setDescription(normalizeDescription(request.description()));

        return BlockResponse.from(blockRepository.save(block));
    }

    @Transactional
    public BlockResponse changeStatus(
            Long hostelId,
            Long blockId,
            BlockStatus status
    ) {
        Block block = getBlock(hostelId, blockId);
        block.setStatus(status);
        return BlockResponse.from(blockRepository.save(block));
    }

    private Block getBlock(Long hostelId, Long blockId) {
        return blockRepository.findByIdAndHostel_Id(blockId, hostelId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Block " + blockId +
                                        " was not found in hostel " + hostelId
                        )
                );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank()
                ? null
                : description.trim();
    }
}

