package com.webapp.bankingplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_actor", columnList = "actorId"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_target", columnList = "targetType,targetId"),
    @Index(name = "idx_audit_created", columnList = "createdAt")
})
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String actorId;

    private String actorRole;

    private String targetType;

    private String targetId;

    @Column(length = 2000)
    private String details;

    private String ipAddress;

    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
