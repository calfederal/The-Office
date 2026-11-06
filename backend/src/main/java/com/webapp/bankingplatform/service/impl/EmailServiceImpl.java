package com.webapp.bankingplatform.service.impl;

import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.UserRepository;
import com.webapp.bankingplatform.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username:noreply@cafed.bank}")
    private String fromAddress;

    @Override
    @Async
    public void sendFromUserId(Long userId, String subject, String body) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty() || user.get().getEmail() == null) {
            log.warn("EmailService: cannot send, user {} has no email", userId);
            return;
        }
        sendTo(user.get().getEmail(), subject, body);
    }

    @Override
    @Async
    public void sendTo(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("EmailService: sent '{}' to {}", subject, to);
        } catch (Exception ex) {
            log.error("EmailService: failed to send '{}' to {}: {}", subject, to, ex.getMessage());
        }
    }
}
