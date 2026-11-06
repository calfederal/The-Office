package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.entity.User;

public interface UserService {
    User registerUser(User user);
    User login(String email, String password);
    User getUserById(Long id);
    User getUserByEmail(String email);
    User updateUserProfile(User user);
    void sendLoginNotification(User user);
    void generateOtp(User user);
    void verifyOtpAndLogin(String phoneNumber, String otp);
}
