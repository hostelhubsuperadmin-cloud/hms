package com.hostelhub.audit.repository;

import com.hostelhub.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository
        extends JpaRepository<AuditEvent, Long> {

    Page<AuditEvent> findByHostelIdOrderByCreatedAtDesc(
            Long hostelId, Pageable pageable
    );

    Page<AuditEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
