
package com.hostelhub.auth.service;

import com.hostelhub.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ActivationResendService {

    private final UserRepository userRepository;
    private final AccountActivationService activationService;

    public ActivationResendService(
            UserRepository userRepository,
            AccountActivationService activationService
    ) {
        this.userRepository = userRepository;
        this.activationService = activationService;
    }

    public void resend(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> user.isPendingActivation())
                .ifPresent(activationService::createAndSendActivationEmail);
    }
}

