package com.hostelhub.audit.service;

import com.hostelhub.audit.entity.AuditEvent;
import com.hostelhub.audit.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            Long actorUserId,
            Long hostelId,
            String action,
            String entityType,
            String entityId,
            String outcome,
            String safeDetails,
            String correlationId) {

        AuditEvent event = new AuditEvent();
        event.setActorUserId(actorUserId);
        event.setHostelId(hostelId);
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setOutcome(outcome);
        event.setDetails(safeDetails);
        event.setCorrelationId(correlationId);

        repository.save(event);
    }
}
