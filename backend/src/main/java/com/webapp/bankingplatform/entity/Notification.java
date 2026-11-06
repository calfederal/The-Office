package com.webapp.bankingplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String type;          // TRANSACTION, SECURITY, ACCOUNT, SYSTEM, MARKETING

    @Column(nullable = false, length = 16)
    private String channel;       // IN_APP, EMAIL, SMS, PUSH

    @Column(length = 255)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false, length = 16)
    private String status;        // UNREAD, READ, ARCHIVED

    @Column(name = "related_type", length = 32)
    private String relatedType;   // TRANSACTION, ACCOUNT, CARD, etc.

    @Column(name = "related_id", length = 64)
    private String relatedId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "UNREAD";
        if (channel == null) channel = "IN_APP";
    }
}
