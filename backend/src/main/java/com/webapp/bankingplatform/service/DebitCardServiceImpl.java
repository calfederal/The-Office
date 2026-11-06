package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.entity.Account;
import com.webapp.bankingplatform.entity.DebitCard;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.AccountRepository;
import com.webapp.bankingplatform.repository.DebitCardRepository;
import com.webapp.bankingplatform.security.PinEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DebitCardServiceImpl implements DebitCardService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "BLOCKED", "EXPIRED", "CANCELLED");
    private static final Set<String> VALID_FEATURES = Set.of("international", "online", "contactless");

    @Autowired
    private DebitCardRepository debitCardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PinEncoder pinEncoder;

    @Override
    public DebitCard issueCard(User caller, Long accountId, String cardType) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        assertOwner(caller, account);

        DebitCard card = new DebitCard();
        card.setCardNumber(generateCardNumber());
        card.setCardholderName(account.getUser().getName());
        card.setCvv(generateCvv());
        card.setIssueDate(LocalDate.now());
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setCardType(cardType != null ? cardType : "STANDARD");
        card.setStatus("ACTIVE");
        card.setDailyLimit(5000.0);
        card.setMonthlyLimit(50000.0);
        card.setInternationalEnabled(false);
        card.setOnlineEnabled(true);
        card.setContactlessEnabled(true);
        card.setPinSet(false);
        card.setFailedPinAttempts(0);
        card.setAccount(account);
        card.setUser(account.getUser());
        return debitCardRepository.save(card);
    }

    @Override
    public DebitCard getCardById(User caller, Long cardId) {
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        assertOwner(caller, card);
        return card;
    }

    @Override
    public List<DebitCard> getCardsByAccount(User caller, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        assertOwner(caller, account);
        return debitCardRepository.findByAccount_Id(accountId);
    }

    @Override
    public List<DebitCard> getCardsByUser(User caller, Long userId) {
        if (!caller.getId().equals(userId) && !"ADMIN".equals(caller.getRole())) {
            throw new RuntimeException("Access denied");
        }
        return debitCardRepository.findByUser_Id(userId);
    }

    @Override
    public DebitCard changePin(User caller, Long cardId, String oldPin, String newPin) {
        DebitCard card = getCardById(caller, cardId);
        if (!"ACTIVE".equals(card.getStatus())) {
            throw new RuntimeException("Card is not active");
        }
        if (card.isPinSet() && !pinEncoder.matches(oldPin, card.getPin())) {
            throw new RuntimeException("Current PIN is incorrect");
        }
        card.setPin(pinEncoder.encode(newPin));
        card.setPinSet(true);
        card.setFailedPinAttempts(0);
        return debitCardRepository.save(card);
    }

    @Override
    public boolean validatePin(User caller, Long cardId, String pin) {
        DebitCard card = getCardById(caller, cardId);
        if (!"ACTIVE".equals(card.getStatus())) {
            throw new RuntimeException("Card is not active");
        }
        if (!card.isPinSet()) {
            throw new RuntimeException("PIN not set");
        }
        if (pinEncoder.matches(pin, card.getPin())) {
            card.setFailedPinAttempts(0);
            debitCardRepository.save(card);
            return true;
        }
        int attempts = card.getFailedPinAttempts() + 1;
        card.setFailedPinAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            card.setStatus("BLOCKED");
        }
        debitCardRepository.save(card);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            throw new RuntimeException("Card blocked due to too many failed PIN attempts");
        }
        return false;
    }

    @Override
    public DebitCard blockCard(User caller, Long cardId, String reason) {
        DebitCard card = getCardById(caller, cardId);
        card.setStatus("BLOCKED");
        return debitCardRepository.save(card);
    }

    @Override
    public DebitCard reportLostOrStolen(User caller, Long cardId, String reason) {
        DebitCard card = getCardById(caller, cardId);
        card.setStatus("CANCELLED");
        return debitCardRepository.save(card);
    }

    @Override
    public DebitCard toggleFeature(User caller, Long cardId, String feature, Boolean enabled) {
        if (feature == null || !VALID_FEATURES.contains(feature.toLowerCase())) {
            throw new RuntimeException("Invalid feature. Allowed: " + VALID_FEATURES);
        }
        DebitCard card = getCardById(caller, cardId);
        switch (feature.toLowerCase()) {
            case "international" -> card.setInternationalEnabled(Boolean.TRUE.equals(enabled));
            case "online" -> card.setOnlineEnabled(Boolean.TRUE.equals(enabled));
            case "contactless" -> card.setContactlessEnabled(Boolean.TRUE.equals(enabled));
        }
        return debitCardRepository.save(card);
    }

    @Override
    public DebitCard updateLimits(User caller, Long cardId, Double dailyLimit, Double monthlyLimit) {
        DebitCard card = getCardById(caller, cardId);
        if (dailyLimit != null) {
            if (dailyLimit <= 0 || dailyLimit > 100000) {
                throw new RuntimeException("Daily limit must be between 1 and 100000");
            }
            card.setDailyLimit(dailyLimit);
        }
        if (monthlyLimit != null) {
            if (monthlyLimit <= 0 || monthlyLimit > 1000000) {
                throw new RuntimeException("Monthly limit must be between 1 and 1000000");
            }
            card.setMonthlyLimit(monthlyLimit);
        }
        return debitCardRepository.save(card);
    }

    private void assertOwner(User caller, Account account) {
        if (account.getUser() == null || !account.getUser().getId().equals(caller.getId())) {
            if (!"ADMIN".equals(caller.getRole())) {
                throw new RuntimeException("Access denied: not account owner");
            }
        }
    }

    private void assertOwner(User caller, DebitCard card) {
        if (card.getUser() == null || !card.getUser().getId().equals(caller.getId())) {
            if (!"ADMIN".equals(caller.getRole())) {
                throw new RuntimeException("Access denied: not card owner");
            }
        }
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private String generateCvv() {
        return String.format("%03d", RANDOM.nextInt(1000));
    }
}
