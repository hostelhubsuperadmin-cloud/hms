package com.hostelhub.allocation.dto;

import java.time.LocalDate;

public record AllocationResponse(
        Long id,
        Long hostelId,

        Long hostlerId,
        String hostlerName,
        String hostlerEmail,

        Long bedId,
        String bedNumber,
        String roomNumber,

        Long allocatedBy,
        String status,

        LocalDate startDate,
        LocalDate plannedCheckoutDate,
        LocalDate endDate
) {
}
