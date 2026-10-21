package com.hostelhub.security;

import java.util.Locale;

import com.hostelhub.auth.dto.LoginRequest;
import com.hostelhub.auth.dto.RegisterRequest;
import com.hostelhub.role.repository.RoleRepository;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hostelhub.auth.service.AccountActivationService;
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AccountActivationService accountActivationService;
    public AuthenticationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            AccountActivationService accountActivationService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.accountActivationService = accountActivationService;
    }


    @Transactional
    public User register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        var defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException(
                        "USER role missing. Check Flyway role seed data."
                ));

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setPhone(request.phone());
        user.markPendingActivation();
        user.addRole(defaultRole);

        final User savedUser;

        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException(
                        "An account with this email already exists."
                );
            }
            throw ex;
        }

        accountActivationService.createAndSendActivationEmail(savedUser);
        return savedUser;
    }


    public Authentication authenticate(LoginRequest request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizeEmail(request.email()),
                        request.password()
                )
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
