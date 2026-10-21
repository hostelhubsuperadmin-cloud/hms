
package com.hostelhub.menu.dto;

import java.math.BigDecimal;

public record HostelMenuItemRequest(
        String dayOfWeek,
        String mealType,
        String name,
        String description,
        BigDecimal price,
        Boolean available
) {}

