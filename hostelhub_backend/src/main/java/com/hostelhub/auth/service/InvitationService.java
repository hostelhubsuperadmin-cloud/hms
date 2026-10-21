
package com.hostelhub.auth.service;

import com.hostelhub.auth.dto.InvitationRequest;
import com.hostelhub.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InvitationService {

    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public InvitationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendInvitation(InvitationRequest request) {
        String message = request.message() == null
                ? ""
                : request.message().trim();

        String body = "Hello,\n\n"
                + "You have been invited to join HostelHub.\n\n"
                + (message.isBlank() ? "" : message + "\n\n")
                + "Visit HostelHub to create your account:\n"
                + frontendUrl + "\n\n"
                + "If you were not expecting this invitation, you can "
                + "ignore this email.";

        emailService.sendText(
                request.email(),
                "You're invited to join HostelHub",
                body
        );
    }
}

