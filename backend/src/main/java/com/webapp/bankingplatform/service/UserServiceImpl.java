package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (user.getPhoneNumber() != null
                && userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("Phone number already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAccountNumber(null);
        user.setAddress(user.getAddress() != null ? user.getAddress() : "");
        user.setCountryCode(user.getCountryCode() != null ? user.getCountryCode() : "US");
        user.setRole(user.getRole() != null ? user.getRole() : "USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return user;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User updateUserProfile(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getName() != null) existingUser.setName(user.getName());
        if (user.getEmail() != null) existingUser.setEmail(user.getEmail());
        if (user.getPhoneNumber() != null) existingUser.setPhoneNumber(user.getPhoneNumber());
        if (user.getAddress() != null) existingUser.setAddress(user.getAddress());
        if (user.getCountryCode() != null) existingUser.setCountryCode(user.getCountryCode());
        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    @Override
    public void sendLoginNotification(User user) {
        // Hook for geolocation-based login notification (Google Maps Services API on classpath).
        // Real implementation: resolve IP -> lat/lng, compare to last known location, email/SMS on anomaly.
    }

    @Override
    public void generateOtp(User user) {
        // Hook for login OTP generation. Real implementation uses java-otp (on classpath).
    }

    @Override
    public void verifyOtpAndLogin(String phoneNumber, String otp) {
        // Hook for OTP-based login verification.
    }
}
