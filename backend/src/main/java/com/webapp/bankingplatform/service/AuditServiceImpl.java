package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.entity.AuditLog;
import com.webapp.bankingplatform.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Async
    public void log(String action, String actorId, String actorRole,
                    String targetType, String targetId, String details) {
        write(action, actorId, actorRole, targetType, targetId, details, null, null);
    }

    @Override
    @Async
    public void log(String action, String actorId, String actorRole,
                    String targetType, String targetId, String details,
                    String ipAddress, String userAgent) {
        write(action, actorId, actorRole, targetType, targetId, details, ipAddress, userAgent);
    }

    private void write(String action, String actorId, String actorRole,
                       String targetType, String targetId, String details,
                       String ipAddress, String userAgent) {
        try {
            AuditLog entry = new AuditLog();
            entry.setAction(action);
            entry.setActorId(actorId);
            entry.setActorRole(actorRole);
            entry.setTargetType(targetType);
            entry.setTargetId(targetId);
            entry.setDetails(details);
            entry.setIpAddress(ipAddress);
            entry.setUserAgent(userAgent);
            auditLogRepository.save(entry);
        } catch (Exception ignored) {
            // Audit logging must never break the calling flow.
        }
    }
}
