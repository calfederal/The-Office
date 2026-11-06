package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.entity.OtpInfo;
import com.webapp.bankingplatform.entity.PasswordResetToken;
import com.webapp.bankingplatform.entity.Token;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.OtpInfoRepository;
import com.webapp.bankingplatform.repository.PasswordResetTokenRepository;
import com.webapp.bankingplatform.repository.TokenRepository;
import com.webapp.bankingplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int RESET_TOKEN_EXPIRY_HOURS = 1;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private OtpInfoRepository otpInfoRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public String generatePasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS));
        resetToken.setUser(user);
        passwordResetTokenRepository.save(resetToken);
        return token;
    }

    @Override
    public void sendOtpForPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Do not leak whether email exists
            return;
        }
        User user = userOpt.get();

        String otp = generateOtp();
        OtpInfo otpInfo = new OtpInfo();
        otpInfo.setPhoneNumber(user.getPhoneNumber());
        otpInfo.setOtp(otp);
        otpInfo.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpInfo.setPurpose("PASSWORD_RESET");
        otpInfo.setUser(user);
        otpInfoRepository.save(otpInfo);

        sendEmail(user.getEmail(), "Password Reset OTP",
            "Your OTP is: " + otp + "\nThis OTP expires in " + OTP_EXPIRY_MINUTES + " minutes.");
    }

    @Override
    public String verifyOtpAndIssueResetToken(String phoneNumber, String otp) {
        Optional<OtpInfo> optionalOtp = otpInfoRepository.findByPhoneNumberAndPurpose(phoneNumber, "PASSWORD_RESET");
        if (optionalOtp.isEmpty()) {
            throw new RuntimeException("OTP not found");
        }
        OtpInfo otpInfo = optionalOtp.get();
        if (otpInfo.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }
        if (!otpInfo.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        // Invalidate OTP after use
        otpInfoRepository.delete(otpInfo);
        return generatePasswordResetToken(otpInfo.getUser());
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }
        PasswordResetToken resetToken = verifyPasswordResetToken(token);
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        // Invalidate all existing tokens after password change
        tokenRepository.findByUser(user).forEach(t -> {
            t.setRevoked(true);
            tokenRepository.save(t);
        });
    }

    @Override
    public void persistToken(User user, String accessToken, String refreshToken) {
        Token access = new Token();
        access.setToken(accessToken);
        access.setUser(user);
        access.setRevoked(false);
        access.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(access);

        Token refresh = new Token();
        refresh.setToken(refreshToken);
        refresh.setUser(user);
        refresh.setRevoked(false);
        refresh.setExpiryDate(LocalDateTime.now().plusDays(30));
        tokenRepository.save(refresh);
    }

    @Override
    public void revokeToken(String token) {
        tokenRepository.findAll().stream()
                .filter(t -> t.getToken().equals(token))
                .forEach(t -> {
                    t.setRevoked(true);
                    tokenRepository.save(t);
                });
    }

    private PasswordResetToken verifyPasswordResetToken(String token) {
        Optional<PasswordResetToken> optionalToken = passwordResetTokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            throw new RuntimeException("Invalid token");
        }
        PasswordResetToken resetToken = optionalToken.get();
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }
        return resetToken;
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
