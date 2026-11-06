package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.entity.Account;
import com.webapp.bankingplatform.entity.Transaction;
import com.webapp.bankingplatform.entity.User;

import java.util.List;

public interface AccountService {
    Account createAccount(User user);
    boolean isPinCreated(User caller, Long accountId);
    Account createPin(User caller, Long accountId, String pin);
    Account updatePin(User caller, Long accountId, String oldPin, String newPin);
    Account cashDeposit(User caller, Long accountId, Double amount);
    Account cashWithdrawal(User caller, Long accountId, Double amount);
    Account fundTransfer(User caller, Long sourceAccountId, Long targetAccountId, Double amount);
    List<Transaction> getTransactions(User caller, Long accountId);
    Account getAccountDetails(User caller, Long accountId);
    Account validatePin(User caller, Long accountId, String pin);
    void validateAmount(Double amount);
    void sendStatement(User caller, Long accountId, String email);
}
