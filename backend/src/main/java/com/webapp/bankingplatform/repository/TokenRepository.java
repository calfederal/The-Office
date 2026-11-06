package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.Token;
import com.webapp.bankingplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByUser(User user);
    boolean existsByUserAndRevokedFalse(User user);
}
