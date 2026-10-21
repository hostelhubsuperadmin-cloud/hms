
package com.hostelhub.block.controller;

import com.hostelhub.block.dto.BlockResponse;
import com.hostelhub.block.dto.CreateBlockRequest;
import com.hostelhub.block.dto.UpdateBlockRequest;
import com.hostelhub.block.entity.BlockStatus;
import com.hostelhub.block.service.BlockService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostels/{hostelId}/blocks")
@PreAuthorize("hasAuthority('HOSTEL_STRUCTURE_MANAGE')")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping
    public BlockResponse create(
            @PathVariable Long hostelId,
            @Valid @RequestBody CreateBlockRequest request
    ) {
        return blockService.create(hostelId, request);
    }

    @GetMapping
    public List<BlockResponse> findAll(
            @PathVariable Long hostelId
    ) {
        return blockService.findAll(hostelId);
    }

    @GetMapping("/{blockId}")
    public BlockResponse findById(
            @PathVariable Long hostelId,
            @PathVariable Long blockId
    ) {
        return blockService.findById(hostelId, blockId);
    }

    @PutMapping("/{blockId}")
    public BlockResponse update(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @Valid @RequestBody UpdateBlockRequest request
    ) {
        return blockService.update(hostelId, blockId, request);
    }

    @PatchMapping("/{blockId}/status")
    public BlockResponse changeStatus(
            @PathVariable Long hostelId,
            @PathVariable Long blockId,
            @RequestParam BlockStatus status
    ) {
        return blockService.changeStatus(hostelId, blockId, status);
    }
}

