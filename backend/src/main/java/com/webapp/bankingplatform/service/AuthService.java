package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.entity.User;

public interface AuthService {
    String generatePasswordResetToken(User user);
    void sendOtpForPasswordReset(String email);
    String verifyOtpAndIssueResetToken(String phoneNumber, String otp);
    void resetPassword(String token, String newPassword);
    void persistToken(User user, String accessToken, String refreshToken);
    void revokeToken(String token);
}
