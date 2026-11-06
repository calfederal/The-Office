package com.webapp.bankingplatform.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory sliding-window rate limiter.
 * For production replace with Redis-backed limiter (e.g. Bucket4j).
 */
@Component
public class RateLimiter {

    private static final int OTP_MAX_PER_HOUR = 5;
    private static final int LOGIN_MAX_PER_15MIN = 10;

    private final ConcurrentHashMap<String, Window> otpWindow = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Window> loginWindow = new ConcurrentHashMap<>();

    public void checkOtpRate(String key) {
        check(key, otpWindow, OTP_MAX_PER_HOUR, 60);
    }

    public void checkLoginRate(String key) {
        check(key, loginWindow, LOGIN_MAX_PER_15MIN, 15);
    }

    private void check(String key, ConcurrentHashMap<String, Window> map, int max, int windowMinutes) {
        LocalDateTime now = LocalDateTime.now();
        Window w = map.computeIfAbsent(key, k -> new Window(now, 0));
        synchronized (w) {
            if (w.start.isBefore(now.minusMinutes(windowMinutes))) {
                w.start = now;
                w.count = 0;
            }
            w.count++;
            if (w.count > max) {
                throw new RuntimeException("Rate limit exceeded. Try again later.");
            }
        }
    }

    private static class Window {
        LocalDateTime start;
        int count;
        Window(LocalDateTime start, int count) {
            this.start = start;
            this.count = count;
        }
    }
}
