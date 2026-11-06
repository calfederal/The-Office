package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.entity.Account;
import com.webapp.bankingplatform.entity.Transaction;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.AccountRepository;
import com.webapp.bankingplatform.repository.TransactionRepository;
import com.webapp.bankingplatform.security.PinEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PinEncoder pinEncoder;

    @Autowired
    private EntityManager entityManager;

    @Value("${app.transfer.max-amount:100000}")
    private double maxTransferAmount;

    @Value("${app.transfer.daily-limit:500000}")
    private double dailyTransferLimit;

    @Override
    public Account createAccount(User user) {
        String accountNumber = generateAccountNumber();
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountType("SAVINGS");
        account.setBalance(0.0);
        account.setBranch("Main Branch");
        account.setIfscCode("BANK0001");
        account.setPinCreated(false);
        account.setUser(user);
        return accountRepository.save(account);
    }

    @Override
    public boolean isPinCreated(User caller, Long accountId) {
        Account account = loadOwnedAccount(caller, accountId);
        return account.isPinCreated();
    }

    @Override
    public Account createPin(User caller, Long accountId, String pin) {
        Account account = loadOwnedAccount(caller, accountId);
        if (account.isPinCreated()) {
            throw new RuntimeException("PIN already created. Use update-pin instead.");
        }
        account.setPin(pinEncoder.encode(pin));
        account.setPinCreated(true);
        return accountRepository.save(account);
    }

    @Override
    public Account updatePin(User caller, Long accountId, String oldPin, String newPin) {
        Account account = loadOwnedAccount(caller, accountId);
        if (!pinEncoder.matches(oldPin, account.getPin())) {
            throw new RuntimeException("Current PIN is incorrect");
        }
        account.setPin(pinEncoder.encode(newPin));
        return accountRepository.save(account);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Account cashDeposit(User caller, Long accountId, Double amount) {
        validateAmount(amount);
        Account account = lockAccount(accountId);
        assertOwner(caller, account);
        assertNotFrozen(account);

        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);
        Account saved = accountRepository.save(account);

        Transaction txn = new Transaction();
        txn.setTransactionType("DEPOSIT");
        txn.setAmount(amount);
        txn.setStatus("COMPLETED");
        txn.setDescription("Cash deposit");
        txn.setSourceAccount(null);
        txn.setTargetAccount(saved);
        txn.setUser(caller);
        txn.setBalanceAfter(newBalance);
        transactionRepository.save(txn);

        return saved;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Account cashWithdrawal(User caller, Long accountId, Double amount) {
        validateAmount(amount);
        Account account = lockAccount(accountId);
        assertOwner(caller, account);
        assertNotFrozen(account);

        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }
        double newBalance = account.getBalance() - amount;
        account.setBalance(newBalance);
        Account saved = accountRepository.save(account);

        Transaction txn = new Transaction();
        txn.setTransactionType("WITHDRAWAL");
        txn.setAmount(amount);
        txn.setStatus("COMPLETED");
        txn.setDescription("Cash withdrawal");
        txn.setSourceAccount(saved);
        txn.setTargetAccount(null);
        txn.setUser(caller);
        txn.setBalanceAfter(newBalance);
        transactionRepository.save(txn);

        return saved;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Account fundTransfer(User caller, Long sourceAccountId, Long targetAccountId, Double amount) {
        validateAmount(amount);
        if (amount > maxTransferAmount) {
            throw new RuntimeException("Transfer amount exceeds per-transaction limit of " + maxTransferAmount);
        }
        if (sourceAccountId.equals(targetAccountId)) {
            throw new RuntimeException("Cannot transfer to same account");
        }

        // Lock both accounts in deterministic order to avoid deadlocks
        Long firstId = Math.min(sourceAccountId, targetAccountId);
        Long secondId = Math.max(sourceAccountId, targetAccountId);
        Account first = lockAccount(firstId);
        Account second = lockAccount(secondId);

        Account source = sourceAccountId.equals(firstId) ? first : second;
        Account target = targetAccountId.equals(firstId) ? first : second;

        assertOwner(caller, source);
        assertNotFrozen(source);
        assertNotFrozen(target);

        if (source.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        // Daily limit check
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        Double todayOut = transactionRepository.sumOutgoingSince(source.getId(), startOfDay);
        if (todayOut != null && todayOut + amount > dailyTransferLimit) {
            throw new RuntimeException("Daily transfer limit exceeded");
        }

        double newSourceBalance = source.getBalance() - amount;
        double newTargetBalance = target.getBalance() + amount;
        source.setBalance(newSourceBalance);
        target.setBalance(newTargetBalance);
        accountRepository.save(source);
        accountRepository.save(target);

        Transaction txn = new Transaction();
        txn.setTransactionType("TRANSFER");
        txn.setAmount(amount);
        txn.setStatus("COMPLETED");
        txn.setDescription("Fund transfer");
        txn.setSourceAccount(source);
        txn.setTargetAccount(target);
        txn.setUser(caller);
        txn.setBalanceAfter(newSourceBalance);
        transactionRepository.save(txn);

        return source;
    }

    @Override
    public List<Transaction> getTransactions(User caller, Long accountId) {
        Account account = loadOwnedAccount(caller, accountId);
        return transactionRepository.findBySourceAccount_AccountNumberOrTargetAccount_AccountNumber(
                account.getAccountNumber(), account.getAccountNumber());
    }

    @Override
    public Account getAccountDetails(User caller, Long accountId) {
        return loadOwnedAccount(caller, accountId);
    }

    @Override
    public Account validatePin(User caller, Long accountId, String pin) {
        Account account = loadOwnedAccount(caller, accountId);
        if (!pinEncoder.matches(pin, account.getPin())) {
            throw new RuntimeException("Invalid PIN");
        }
        return account;
    }

    @Override
    public void validateAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        if (amount % 100 != 0) {
            throw new RuntimeException("Amount must be a multiple of 100");
        }
    }

    @Override
    public void sendStatement(User caller, Long accountId, String email) {
        Account account = loadOwnedAccount(caller, accountId);
        // Hook: generate PDF statement and email it. Real impl uses OpenPDF or iText + JavaMailSender.
        // Statement should include: account summary, period transactions, fees, interest, balance.
    }

    private Account lockAccount(Long accountId) {
        return entityManager.find(Account.class, accountId, LockModeType.PESSIMISTIC_WRITE);
    }

    private Account loadOwnedAccount(User caller, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        assertOwner(caller, account);
        return account;
    }

    private void assertOwner(User caller, Account account) {
        if (account.getUser() == null || !account.getUser().getId().equals(caller.getId())) {
            // Allow ADMIN role bypass
            boolean isAdmin = "ADMIN".equals(caller.getRole());
            if (!isAdmin) {
                throw new RuntimeException("Access denied: not account owner");
            }
        }
    }

    private void assertNotFrozen(Account account) {
        if (account.isFrozen()) {
            throw new RuntimeException("Account is frozen. Contact support.");
        }
    }

    private String generateAccountNumber() {
        return "BANK" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
