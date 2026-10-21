
package com.hostelhub.admin.dto;

import java.time.OffsetDateTime;
import java.util.Set;

public record AdminUserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String status,
        boolean emailVerified,
        Set<String> roles,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

