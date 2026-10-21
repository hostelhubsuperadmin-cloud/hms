
package com.hostelhub.hostel.dto;

public record HostelAdminResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String status,
        boolean emailVerified
) {
}

