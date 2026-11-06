package com.webapp.bankingplatform.controller;

import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.security.JwtService;
import com.webapp.bankingplatform.security.RateLimiter;
import com.webapp.bankingplatform.service.AuthService;
import com.webapp.bankingplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RateLimiter rateLimiter;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> request) {
        try {
            User user = new User();
            user.setName((String) request.get("name"));
            user.setEmail((String) request.get("email"));
            user.setPassword((String) request.get("password"));
            user.setPhoneNumber((String) request.get("phoneNumber"));
            user.setCountryCode((String) request.getOrDefault("countryCode", "US"));
            user.setAddress((String) request.getOrDefault("address", ""));

            User registered = userService.registerUser(user);
            Map<String, Object> tokens = issueTokens(registered);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("user", sanitize(registered));
            response.putAll(tokens);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            rateLimiter.checkLoginRate(email);

            User user = userService.login(email, password);
            Map<String, Object> tokens = issueTokens(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", sanitize(user));
            response.putAll(tokens);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, Object> request) {
        try {
            String refreshToken = (String) request.get("refreshToken");
            String email = jwtService.extractUsername(refreshToken);
            User user = userService.getUserByEmail(email);
            if (user == null || !jwtService.isTokenValid(refreshToken, email)) {
                throw new RuntimeException("Invalid refresh token");
            }
            Map<String, Object> tokens = issueTokens(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.putAll(tokens);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authService.revokeToken(token);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Logged out");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            rateLimiter.checkOtpRate(email);
            authService.sendOtpForPasswordReset(email);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "If the email exists, an OTP has been sent");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, Object> request) {
        try {
            String phoneNumber = (String) request.get("phoneNumber");
            String otp = (String) request.get("otp");
            String resetToken = authService.verifyOtpAndIssueResetToken(phoneNumber, otp);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resetToken", resetToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> request) {
        try {
            String token = (String) request.get("token");
            String newPassword = (String) request.get("newPassword");
            authService.resetPassword(token, newPassword);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof User user)) {
                throw new RuntimeException("Not authenticated");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", sanitize(user));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Map<String, Object> issueTokens(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("role", user.getRole() != null ? user.getRole() : "USER");
        String access = jwtService.generateAccessToken(user.getEmail(), claims);
        String refresh = jwtService.generateRefreshToken(user.getEmail());
        authService.persistToken(user, access, refresh);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", access);
        tokens.put("refreshToken", refresh);
        tokens.put("tokenType", "Bearer");
        return tokens;
    }

    private Map<String, Object> sanitize(User user) {
        Map<String, Object> u = new HashMap<>();
        u.put("id", user.getId());
        u.put("name", user.getName());
        u.put("email", user.getEmail());
        u.put("phoneNumber", user.getPhoneNumber());
        u.put("countryCode", user.getCountryCode());
        u.put("address", user.getAddress());
        u.put("accountNumber", user.getAccountNumber());
        u.put("role", user.getRole());
        u.put("createdAt", user.getCreatedAt());
        return u;
    }

    private ResponseEntity<Map<String, Object>> errorResponse(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
