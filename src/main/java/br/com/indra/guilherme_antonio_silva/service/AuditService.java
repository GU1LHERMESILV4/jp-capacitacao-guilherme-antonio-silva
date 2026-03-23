package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.model.AuditLog;
import br.com.indra.guilherme_antonio_silva.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void logChange(String entityType,
                          String entityId,
                          String action,
                          Object before,
                          Object after,
                          String who) {
        AuditLog log = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .beforeJson(toJsonSafe(before))
                .afterJson(toJsonSafe(after))
                .who(who)
                .when(OffsetDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    private String toJsonSafe(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"serialization_failed\"}";
        }
    }
}

