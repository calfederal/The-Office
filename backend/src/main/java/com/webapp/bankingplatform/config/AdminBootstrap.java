package com.webapp.bankingplatform.config;

import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Creates a default ADMIN account on first startup if no admin exists.
 * Credentials are configurable via app.admin.bootstrap-email / app.admin.bootstrap-password.
 * Disable in production by setting app.admin.bootstrap-enabled=false.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap-enabled:true}")
    private boolean enabled;

    @Value("${app.admin.bootstrap-email:admin@cafed.bank}")
    private String adminEmail;

    @Value("${app.admin.bootstrap-password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Set app.admin.bootstrap-password before enabling admin bootstrap.");
        }
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> "ADMIN".equalsIgnoreCase(u.getRole()));
        if (adminExists) {
            log.info("AdminBootstrap: ADMIN user already exists, skipping.");
            return;
        }
        User admin = new User();
        admin.setName("System Administrator");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        admin.setLocked(false);
        admin.setKycStatus("APPROVED");
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);
        log.warn("AdminBootstrap: created default ADMIN user '{}' - change the password immediately.", adminEmail);
    }
}
