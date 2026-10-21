package com.hostelhub.auth.controller;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hostelhub.auth.dto.LoginRequest;
import com.hostelhub.auth.dto.LoginResponse;
import com.hostelhub.auth.dto.RegisterRequest;
import com.hostelhub.auth.service.AccountActivationService;
import com.hostelhub.security.AuthenticationService;
import com.hostelhub.security.CustomUserDetails;
import com.hostelhub.security.JwtService;
import com.hostelhub.security.SecurityConstants;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final com.hostelhub.auth.service.AccountActivationService
            accountActivationService;
    @Value("${COOKIE_SECURE:false}")
    private boolean cookieSecure;

    public AuthController(
            AuthenticationService authenticationService,
            JwtService jwtService,
            UserRepository userRepository,
            AccountActivationService
                    accountActivationService
    ) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.accountActivationService = accountActivationService;
    }
    @GetMapping("/activate")
    public ResponseEntity<Map<String, String>> activate(
            @RequestParam("token") String token
    ) {
        accountActivationService.activate(token);

        return ResponseEntity.ok(Map.of(
                "message",
                "Email activated successfully. You can now log in."
        ));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(
                Map.of(
                        "headerName", csrfToken.getHeaderName(),
                        "parameterName", csrfToken.getParameterName(),
                        "token", csrfToken.getToken()
                )
        );
    }


    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = authenticationService.register(request);

        return ResponseEntity.status(201).body(toResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet())
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication =
                authenticationService.authenticate(request);

        CustomUserDetails principal =
                (CustomUserDetails) authentication.getPrincipal();

        String jwt = jwtService.generateToken(principal.getUsername());

        ResponseCookie cookie = ResponseCookie
                .from(SecurityConstants.ACCESS_COOKIE, jwt)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(jwtService.getExpirationSeconds()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new LoginResponse(
                principal.getId(),
                principal.getFullName(),
                principal.getUsername(),
                principal.getRoleNames()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(SecurityConstants.ACCESS_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public LoginResponse me(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return new LoginResponse(
                principal.getId(),
                principal.getFullName(),
                principal.getUsername(),
                principal.getRoleNames()
        );
    }

    private LoginResponse toResponse(
            Long id,
            String fullName,
            String email,
            Set<String> roles
    ) {
        return new LoginResponse(id, fullName, email, roles);
    }
}