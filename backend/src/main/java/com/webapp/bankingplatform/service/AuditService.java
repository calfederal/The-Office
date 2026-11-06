package com.webapp.bankingplatform.service;

public interface AuditService {

    void log(String action, String actorId, String actorRole,
             String targetType, String targetId, String details);

    void log(String action, String actorId, String actorRole,
             String targetType, String targetId, String details,
             String ipAddress, String userAgent);
}
