
package com.hostelhub.auth.controller;

import com.hostelhub.auth.dto.InvitationRequest;
import com.hostelhub.auth.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> invite(
            @Valid @RequestBody InvitationRequest request
    ) {
        invitationService.sendInvitation(request);

        return ResponseEntity.ok(Map.of(
                "message", "Invitation email sent."
        ));
    }
}

