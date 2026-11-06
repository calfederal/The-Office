package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.DebitCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {
    Optional<DebitCard> findByCardNumber(String cardNumber);
    List<DebitCard> findByAccount_Id(Long accountId);
    List<DebitCard> findByUser_Id(Long userId);
}
