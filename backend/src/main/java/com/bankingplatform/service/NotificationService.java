package com.bankingplatform.service;

import com.bankingplatform.dto.NotificationPreferenceRequest;
import com.bankingplatform.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /** Send an in-app notification (always). Optionally fan out to email/SMS based on user prefs. */
    void notify(Long userId, String type, String subject, String body,
                String relatedType, String relatedId);

    /** Send to a specific channel regardless of preferences (admin/system use). */
    void send(Long userId, String type, String channel, String subject, String body,
              String relatedType, String relatedId);

    Page<NotificationResponse> list(Long userId, String status, Pageable pageable);

    long unreadCount(Long userId);

    boolean markRead(Long id, Long userId);

    int markAllRead(Long userId);

    void savePreference(Long userId, NotificationPreferenceRequest req);
}
