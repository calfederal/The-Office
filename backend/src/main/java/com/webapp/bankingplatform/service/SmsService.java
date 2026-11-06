package com.webapp.bankingplatform.service;

public interface SmsService {
    /** Send an SMS to the user identified by userId (resolves phone). */
    void sendFromUserId(Long userId, String subject, String body);

    /** Send to an explicit phone number. */
    void sendTo(String phoneNumber, String body);
}
