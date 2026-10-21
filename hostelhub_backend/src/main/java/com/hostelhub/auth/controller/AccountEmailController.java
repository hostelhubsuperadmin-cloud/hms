
package com.hostelhub.auth.controller;

import java.util.Map;

import com.hostelhub.auth.dto.ActivateAccountRequest;
import com.hostelhub.auth.dto.ChangePasswordRequest;
import com.hostelhub.auth.dto.ForgotPasswordRequest;
import com.hostelhub.auth.dto.ResendActivationRequest;
import com.hostelhub.auth.dto.ResetPasswordRequest;
import com.hostelhub.auth.dto.VerifyOtpRequest;
import com.hostelhub.auth.service.AccountActivationService;
import com.hostelhub.auth.service.ActivationResendService;
import com.hostelhub.auth.service.ChangePasswordService;
import com.hostelhub.auth.service.PasswordResetService;
import com.hostelhub.security.CustomUserDetails;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AccountEmailController {

    private final AccountActivationService accountActivationService;
    private final ActivationResendService activationResendService;
    private final PasswordResetService passwordResetService;
    private final ChangePasswordService changePasswordService;

    public AccountEmailController(
            AccountActivationService accountActivationService,
            ActivationResendService activationResendService,
            PasswordResetService passwordResetService,
            ChangePasswordService changePasswordService
    ) {
        this.accountActivationService = accountActivationService;
        this.activationResendService = activationResendService;
        this.passwordResetService = passwordResetService;
        this.changePasswordService = changePasswordService;
    }

    @PostMapping("/activate")
    public ResponseEntity<Map<String, String>> activateAndSetPassword(
            @Valid @RequestBody ActivateAccountRequest request
    ) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match."
            );
        }

        accountActivationService.activateAndSetPassword(
                request.token(),
                request.newPassword()
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "Account activated and password set successfully. You can now log in."
        ));
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<Map<String, String>> resendActivation(
            @Valid @RequestBody ResendActivationRequest request
    ) {
        activationResendService.resend(request.email());

        return ResponseEntity.ok(Map.of(
                "message",
                "If the account requires activation, an email will be sent."
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.requestOtp(request.email());

        return ResponseEntity.ok(Map.of(
                "message",
                "If an eligible account exists, a password-reset OTP will be sent."
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to change your password."
            );
        }

        changePasswordService.changePassword(
                principal.getId(),
                request.currentPassword(),
                request.newPassword()
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "Password changed successfully."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.resetPassword(
                request.resetToken(),
                request.newPassword()
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "Password reset successfully. Please log in."
        ));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        String resetToken = passwordResetService.verifyOtp(
                request.email(),
                request.otp()
        );

        return ResponseEntity.ok(Map.of(
                "resetToken", resetToken,
                "message", "OTP verified. Continue to reset your password."
        ));
    }
}


