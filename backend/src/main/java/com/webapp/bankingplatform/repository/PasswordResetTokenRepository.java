package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.PasswordResetToken;
import com.webapp.bankingplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserAndExpiryDateAfter(User user, java.time.LocalDateTime expiryDate);
}
