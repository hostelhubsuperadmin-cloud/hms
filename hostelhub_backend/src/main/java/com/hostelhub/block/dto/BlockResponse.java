
package com.hostelhub.block.dto;

import com.hostelhub.block.entity.Block;
import com.hostelhub.block.entity.BlockStatus;
import java.time.Instant;

public record BlockResponse(
        Long id,
        Long hostelId,
        String name,
        String code,
        String description,
        BlockStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static BlockResponse from(Block block) {
        return new BlockResponse(
                block.getId(),
                block.getHostel().getId(),
                block.getName(),
                block.getCode(),
                block.getDescription(),
                block.getStatus(),
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }
}

