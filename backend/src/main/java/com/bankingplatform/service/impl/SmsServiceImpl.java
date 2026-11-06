package com.bankingplatform.service.impl;

import com.bankingplatform.entity.User;
import com.bankingplatform.repository.UserRepository;
import com.bankingplatform.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * SMS provider stub. In production, wire to Twilio / Vonage / Bandwidth.
 * For now, logs the message so the call sites work end-to-end.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    private final UserRepository userRepository;

    @Value("${app.sms.enabled:false}")
    private boolean enabled;

    @Value("${app.sms.provider:LOG}")
    private String provider;

    @Override
    @Async
    public void sendFromUserId(Long userId, String subject, String body) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty() || user.get().getPhoneNumber() == null) {
            log.warn("SmsService: cannot send, user {} has no phone", userId);
            return;
        }
        sendTo(user.get().getPhoneNumber(), body);
    }

    @Override
    @Async
    public void sendTo(String phoneNumber, String body) {
        if (!enabled) {
            log.info("SmsService[{}] (disabled, logging only) -> {}: {}", provider, phoneNumber, body);
            return;
        }
        // TODO: integrate Twilio / Vonage here.
        log.info("SmsService[{}] -> {}: {}", provider, phoneNumber, body);
    }
}
