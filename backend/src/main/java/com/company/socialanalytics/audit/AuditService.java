package com.company.socialanalytics.audit;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(UUID actorUserId, String action, String details) {
        auditLogRepository.save(new AuditLog(actorUserId, action, details));
    }
}
