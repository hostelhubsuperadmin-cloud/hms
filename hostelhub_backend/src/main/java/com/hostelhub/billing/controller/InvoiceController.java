package com.hostelhub.billing.controller;

import com.hostelhub.billing.dto.InvoiceResponse;
import com.hostelhub.billing.service.InvoiceService;
import com.hostelhub.security.AuthenticatedHostlerId;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/invoices")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @GetMapping("/mine")
    public List<InvoiceResponse> mine(Authentication authentication) {
        /*
         * Resolve the hostler ID from the authenticated user on the server.
         * Do not accept an arbitrary hostlerId query parameter here.
         */
        Long hostlerId = AuthenticatedHostlerId.from(authentication);
        return service.listForHostler(hostlerId);
    }
}
