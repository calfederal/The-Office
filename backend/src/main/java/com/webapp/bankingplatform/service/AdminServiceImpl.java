package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.dto.AdminAccountResponse;
import com.webapp.bankingplatform.dto.AdminTransactionResponse;
import com.webapp.bankingplatform.dto.AdminUserResponse;
import com.webapp.bankingplatform.dto.PageResponse;
import com.webapp.bankingplatform.entity.Account;
import com.webapp.bankingplatform.entity.Transaction;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.AccountRepository;
import com.webapp.bankingplatform.repository.TransactionRepository;
import com.webapp.bankingplatform.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN", "AUDITOR");
    private static final Set<String> ALLOWED_KYC = Set.of("PENDING", "APPROVED", "REJECTED", "EXPIRED");

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    public AdminServiceImpl(UserRepository userRepository,
                            AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            AuditService auditService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    // ---------- Users ----------

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> listUsers(String search, String role, String kycStatus, Pageable pageable) {
        Specification<User> spec = (root, q, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("email")), like),
                cb.like(cb.lower(root.get("phoneNumber")), like)
            ));
        }
        if (role != null && !role.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("role"), role));
        }
        if (kycStatus != null && !kycStatus.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("kycStatus"), kycStatus));
        }
        Page<User> page = userRepository.findAll(spec, pageable);
        return PageResponse.from(page, AdminUserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long id) {
        return AdminUserResponse.from(loadUser(id));
    }

    @Override
    @Transactional
    public AdminUserResponse setEnabled(Long id, boolean enabled) {
        User u = loadUser(id);
        u.setEnabled(enabled);
        User saved = userRepository.save(u);
        auditService.log("USER_ENABLED", String.valueOf(id), "ADMIN", "User", String.valueOf(id),
                "enabled=" + enabled);
        return AdminUserResponse.from(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse setLocked(Long id, boolean locked) {
        User u = loadUser(id);
        u.setLocked(locked);
        User saved = userRepository.save(u);
        auditService.log("USER_LOCKED", String.valueOf(id), "ADMIN", "User", String.valueOf(id),
                "locked=" + locked);
        return AdminUserResponse.from(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse setRole(Long id, String role) {
        if (role == null || !ALLOWED_ROLES.contains(role.toUpperCase())) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        User u = loadUser(id);
        String prev = u.getRole();
        u.setRole(role.toUpperCase());
        User saved = userRepository.save(u);
        auditService.log("USER_ROLE_CHANGED", String.valueOf(id), "ADMIN", "User", String.valueOf(id),
                "from=" + prev + " to=" + role.toUpperCase());
        return AdminUserResponse.from(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse setKycStatus(Long id, String kycStatus) {
        if (kycStatus == null || !ALLOWED_KYC.contains(kycStatus.toUpperCase())) {
            throw new IllegalArgumentException("Invalid KYC status: " + kycStatus);
        }
        User u = loadUser(id);
        String prev = u.getKycStatus();
        u.setKycStatus(kycStatus.toUpperCase());
        User saved = userRepository.save(u);
        auditService.log("KYC_STATUS_CHANGED", String.valueOf(id), "ADMIN", "User", String.valueOf(id),
                "from=" + prev + " to=" + kycStatus.toUpperCase());
        return AdminUserResponse.from(saved);
    }

    // ---------- Accounts ----------

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminAccountResponse> listAccounts(String search, String accountType, Boolean frozen, Pageable pageable) {
        Specification<Account> spec = (root, q, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("accountNumber")), like),
                cb.like(cb.lower(root.get("branch")), like),
                cb.like(cb.lower(root.get("ifscCode")), like)
            ));
        }
        if (accountType != null && !accountType.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("accountType"), accountType));
        }
        if (frozen != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("frozen"), frozen));
        }
        Page<Account> page = accountRepository.findAll(spec, pageable);
        return PageResponse.from(page, AdminAccountResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAccountResponse getAccount(Long id) {
        return AdminAccountResponse.from(loadAccount(id));
    }

    @Override
    @Transactional
    public AdminAccountResponse setFrozen(Long id, boolean frozen) {
        Account a = loadAccount(id);
        a.setFrozen(frozen);
        Account saved = accountRepository.save(a);
        auditService.log("ACCOUNT_FROZEN", String.valueOf(id), "ADMIN", "Account", String.valueOf(id),
                "frozen=" + frozen);
        return AdminAccountResponse.from(saved);
    }

    // ---------- Transactions ----------

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminTransactionResponse> listTransactions(
            String accountNumber, String type, String status,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<Transaction> spec = (root, q, cb) -> cb.conjunction();
        if (accountNumber != null && !accountNumber.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.or(
                cb.equal(root.get("sourceAccount").get("accountNumber"), accountNumber),
                cb.equal(root.get("targetAccount").get("accountNumber"), accountNumber)
            ));
        }
        if (type != null && !type.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("transactionType"), type));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), to));
        }
        Page<Transaction> page = transactionRepository.findAll(spec, pageable);
        return PageResponse.from(page, AdminTransactionResponse::from);
    }

    // ---------- KYC queue ----------

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> kycQueue(Pageable pageable) {
        Page<User> page = userRepository.findAll(
            (root, q, cb) -> cb.equal(root.get("kycStatus"), "PENDING"),
            pageable);
        return PageResponse.from(page, AdminUserResponse::from);
    }

    // ---------- helpers ----------

    private User loadUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    private Account loadAccount(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Account not found: " + id));
    }
}
