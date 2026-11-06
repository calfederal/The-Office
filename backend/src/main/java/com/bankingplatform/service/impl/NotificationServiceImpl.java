package com.bankingplatform.service.impl;

import com.bankingplatform.dto.NotificationPreferenceRequest;
import com.bankingplatform.dto.NotificationResponse;
import com.bankingplatform.entity.Notification;
import com.bankingplatform.entity.NotificationPreference;
import com.bankingplatform.repository.NotificationPreferenceRepository;
import com.bankingplatform.repository.NotificationRepository;
import com.bankingplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Override
    @Transactional
    public void notify(Long userId, String type, String subject, String body,
                       String relatedType, String relatedId) {
        // Always create the in-app record.
        send(userId, type, "IN_APP", subject, body, relatedType, relatedId);

        // Fan out to email/SMS based on user preferences.
        List<NotificationPreference> prefs = preferenceRepository.findByUserId(userId);
        boolean emailOn = prefs.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(type) || p.getCategory().equalsIgnoreCase("ALL"))
                .findFirst()
                .map(NotificationPreference::isEmailEnabled)
                .orElse(true);
        boolean smsOn = prefs.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(type) || p.getCategory().equalsIgnoreCase("ALL"))
                .findFirst()
                .map(NotificationPreference::isSmsEnabled)
                .orElse(false);

        if (emailOn) {
            send(userId, type, "EMAIL", subject, body, relatedType, relatedId);
        }
        if (smsOn) {
            send(userId, type, "SMS", subject, body, relatedType, relatedId);
        }
    }

    @Override
    @Transactional
    public void send(Long userId, String type, String channel, String subject, String body,
                     String relatedType, String relatedId) {
        Notification n = Notification.builder()
                .userId(userId)
                .type(type)
                .channel(channel)
                .subject(subject)
                .body(body)
                .status("UNREAD")
                .relatedType(relatedType)
                .relatedId(relatedId)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(n);

        if ("EMAIL".equalsIgnoreCase(channel)) {
            // Email needs the address; EmailService resolves it from userId.
            emailService.sendFromUserId(userId, subject, body);
        } else if ("SMS".equalsIgnoreCase(channel)) {
            smsService.sendFromUserId(userId, subject, body);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(Long userId, String status, Pageable pageable) {
        Page<Notification> page = (status == null || status.isBlank())
                ? notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        return page.map(NotificationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, "UNREAD");
    }

    @Override
    @Transactional
    public boolean markRead(Long id, Long userId) {
        int updated = notificationRepository.markRead(id, userId, LocalDateTime.now());
        return updated > 0;
    }

    @Override
    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void savePreference(Long userId, NotificationPreferenceRequest req) {
        NotificationPreference pref = preferenceRepository
                .findByUserIdAndCategory(userId, req.getCategory())
                .orElseGet(() -> NotificationPreference.builder()
                        .userId(userId)
                        .category(req.getCategory())
                        .build());
        pref.setEmailEnabled(req.isEmailEnabled());
        pref.setSmsEnabled(req.isSmsEnabled());
        pref.setPushEnabled(req.isPushEnabled());
        pref.setInAppEnabled(req.isInAppEnabled());
        preferenceRepository.save(pref);
    }
}
