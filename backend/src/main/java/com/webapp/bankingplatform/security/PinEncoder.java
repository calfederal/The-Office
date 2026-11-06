package com.webapp.bankingplatform.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PinEncoder {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public String encode(String rawPin) {
        if (rawPin == null || !rawPin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("PIN must be 4-6 digits");
        }
        return encoder.encode(rawPin);
    }

    public boolean matches(String rawPin, String hashedPin) {
        if (rawPin == null || hashedPin == null) {
            return false;
        }
        try {
            return encoder.matches(rawPin, hashedPin);
        } catch (Exception e) {
            return false;
        }
    }
}
