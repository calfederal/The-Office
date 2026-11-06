package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.entity.DebitCard;
import com.webapp.bankingplatform.entity.User;

import java.util.List;

public interface DebitCardService {
    DebitCard issueCard(User caller, Long accountId, String cardType);
    DebitCard getCardById(User caller, Long cardId);
    List<DebitCard> getCardsByAccount(User caller, Long accountId);
    List<DebitCard> getCardsByUser(User caller, Long userId);
    DebitCard changePin(User caller, Long cardId, String oldPin, String newPin);
    boolean validatePin(User caller, Long cardId, String pin);
    DebitCard blockCard(User caller, Long cardId, String reason);
    DebitCard reportLostOrStolen(User caller, Long cardId, String reason);
    DebitCard toggleFeature(User caller, Long cardId, String feature, Boolean enabled);
    DebitCard updateLimits(User caller, Long cardId, Double dailyLimit, Double monthlyLimit);
}
