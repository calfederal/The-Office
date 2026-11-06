package com.webapp.bankingplatform.service;

public interface EmailService {
    /** Send a templated email to the user identified by userId (resolves email address). */
    void sendFromUserId(Long userId, String subject, String body);

    /** Send to an explicit address. */
    void sendTo(String to, String subject, String body);
}
