package com.webapp.bankingplatform.controller;

import com.webapp.bankingplatform.dto.AdminAccountResponse;
import com.webapp.bankingplatform.dto.AdminTransactionResponse;
import com.webapp.bankingplatform.dto.AdminUserResponse;
import com.webapp.bankingplatform.dto.AuditLogResponse;
import com.webapp.bankingplatform.dto.PageResponse;
import com.webapp.bankingplatform.entity.AuditLog;
import com.webapp.bankingplatform.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import com.webapp.bankingplatform.service.AdminService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuditLogRepository auditLogRepository;

    public AdminController(AdminService adminService, AuditLogRepository auditLogRepository) {
        this.adminService = adminService;
        this.auditLogRepository = auditLogRepository;
    }

    // ---------- Users ----------

    @GetMapping("/users")
    public PageResponse<AdminUserResponse> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String kycStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {
        return adminService.listUsers(search, role, kycStatus, pageable(page, size, sort));
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUser(@PathVariable Long id) {
        return adminService.getUser(id);
    }

    @PatchMapping("/users/{id}/enabled")
    public AdminUserResponse setEnabled(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return adminService.setEnabled(id, body.getOrDefault("enabled", true));
    }

    @PatchMapping("/users/{id}/locked")
    public AdminUserResponse setLocked(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return adminService.setLocked(id, body.getOrDefault("locked", false));
    }

    @PatchMapping("/users/{id}/role")
    public AdminUserResponse setRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return adminService.setRole(id, body.get("role"));
    }

    @PatchMapping("/users/{id}/kyc")
    public AdminUserResponse setKycStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return adminService.setKycStatus(id, body.get("kycStatus"));
    }

    // ---------- Accounts ----------

    @GetMapping("/accounts")
    public PageResponse<AdminAccountResponse> listAccounts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) Boolean frozen,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {
        return adminService.listAccounts(search, accountType, frozen, pageable(page, size, sort));
    }

    @GetMapping("/accounts/{id}")
    public AdminAccountResponse getAccount(@PathVariable Long id) {
        return adminService.getAccount(id);
    }

    @PatchMapping("/accounts/{id}/frozen")
    public AdminAccountResponse setFrozen(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return adminService.setFrozen(id, body.getOrDefault("frozen", false));
    }

    // ---------- Transactions ----------

    @GetMapping("/transactions")
    public PageResponse<AdminTransactionResponse> listTransactions(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String[] sort) {
        return adminService.listTransactions(accountNumber, type, status, from, to,
            pageable(page, size, sort));
    }

    // ---------- Audit log ----------

    @GetMapping("/audit")
    public PageResponse<AuditLogResponse> listAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        Specification<AuditLog> spec = (root, q, cb) -> {
            Predicate p = cb.conjunction();
            if (action != null && !action.isBlank()) p = cb.and(p, cb.equal(root.get("action"), action));
            if (actorId != null && !actorId.isBlank()) p = cb.and(p, cb.equal(root.get("actorId"), actorId));
            if (targetType != null && !targetType.isBlank()) p = cb.and(p, cb.equal(root.get("targetType"), targetType));
            if (targetId != null && !targetId.isBlank()) p = cb.and(p, cb.equal(root.get("targetId"), targetId));
            if (from != null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) p = cb.and(p, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            return p;
        };
        var pg = auditLogRepository.findAll(spec, pageable(page, size, sort));
        return PageResponse.from(pg, AuditLogResponse::from);
    }

    // ---------- KYC ----------

    @GetMapping("/kyc/queue")
    public PageResponse<AdminUserResponse> kycQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        return adminService.kycQueue(pageable(page, size, sort));
    }

    // ---------- helpers ----------

    private Pageable pageable(int page, int size, String[] sort) {
        int safeSize = Math.min(Math.max(size, 1), 200);
        Sort s = Sort.by(Sort.Direction.DESC, "id");
        if (sort != null && sort.length > 0) {
            String field = sort[0];
            Sort.Direction dir = (sort.length > 1 && "asc".equalsIgnoreCase(sort[1]))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            s = Sort.by(dir, field);
        }
        return PageRequest.of(Math.max(page, 0), safeSize, s);
    }
}
