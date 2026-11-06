package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.dto.AdminAccountResponse;
import com.webapp.bankingplatform.dto.AdminTransactionResponse;
import com.webapp.bankingplatform.dto.AdminUserResponse;
import com.webapp.bankingplatform.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AdminService {

    // User management
    PageResponse<AdminUserResponse> listUsers(String search, String role, String kycStatus, Pageable pageable);
    AdminUserResponse getUser(Long id);
    AdminUserResponse setEnabled(Long id, boolean enabled);
    AdminUserResponse setLocked(Long id, boolean locked);
    AdminUserResponse setRole(Long id, String role);
    AdminUserResponse setKycStatus(Long id, String kycStatus);

    // Account management
    PageResponse<AdminAccountResponse> listAccounts(String search, String accountType, Boolean frozen, Pageable pageable);
    AdminAccountResponse getAccount(Long id);
    AdminAccountResponse setFrozen(Long id, boolean frozen);

    // Transaction oversight
    PageResponse<AdminTransactionResponse> listTransactions(
            String accountNumber, String type, String status,
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    // KYC review queue
    PageResponse<AdminUserResponse> kycQueue(Pageable pageable);
}
