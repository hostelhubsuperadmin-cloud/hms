package com.hostelhub.auth.dto;

import java.util.Set;

public record LoginResponse(
        Long id,
        String fullName,
        String email,
        Set<String> roles
) {}
