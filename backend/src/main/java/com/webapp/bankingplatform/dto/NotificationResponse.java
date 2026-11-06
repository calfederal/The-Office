package com.webapp.bankingplatform.dto;

import com.webapp.bankingplatform.entity.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String channel;
    private String subject;
    private String body;
    private String status;
    private String relatedType;
    private String relatedId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .channel(n.getChannel())
                .subject(n.getSubject())
                .body(n.getBody())
                .status(n.getStatus())
                .relatedType(n.getRelatedType())
                .relatedId(n.getRelatedId())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}
