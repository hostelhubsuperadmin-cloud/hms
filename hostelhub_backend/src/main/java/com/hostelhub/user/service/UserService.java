package com.hostelhub.user.service;

import java.util.stream.Collectors;

import com.hostelhub.common.exception.ResourceNotFoundException;
import com.hostelhub.security.SecurityUtils;
import com.hostelhub.user.dto.UserResponse;
import com.hostelhub.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public UserService(
            UserRepository userRepository,
            SecurityUtils securityUtils
    ) {
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        var user = userRepository.findById(securityUtils.currentUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found.")
                );

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus(),
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet())
        );
    }
}
