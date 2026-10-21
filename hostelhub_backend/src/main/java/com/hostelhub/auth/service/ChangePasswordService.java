

package com.hostelhub.auth.service;

import com.hostelhub.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(
            Long authenticatedUserId,
            String currentPassword,
            String newPassword
    ) {
        var user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found."
                ));

        if (!passwordEncoder.matches(
                currentPassword, user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }

        if (passwordEncoder.matches(
                newPassword, user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "New password must differ from the current password."
            );
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

