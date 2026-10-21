package com.hostelhub.audit.controller;

import com.hostelhub.audit.entity.AuditEvent;
import com.hostelhub.audit.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {

    private final AuditEventRepository repository;

    public AuditController(AuditEventRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    public Page<AuditEvent> list(
            @RequestParam(required = false) Long hostelId,
            @PageableDefault(size = 25, sort = "createdAt")
            Pageable pageable) {

        if (hostelId != null) {
            return repository.findByHostelIdOrderByCreatedAtDesc(
                    hostelId, pageable
            );
        }
        return repository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
