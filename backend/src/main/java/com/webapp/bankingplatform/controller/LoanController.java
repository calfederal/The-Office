package com.webapp.bankingplatform.controller;

import com.webapp.bankingplatform.dto.LoanApplicationRequest;
import com.webapp.bankingplatform.dto.LoanPaymentRequest;
import com.webapp.bankingplatform.dto.LoanPaymentResponse;
import com.webapp.bankingplatform.dto.LoanResponse;
import com.webapp.bankingplatform.entity.Loan;
import com.webapp.bankingplatform.repository.UserRepository;
import com.webapp.bankingplatform.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoanResponse> apply(@Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(loanService.applyForLoan(currentUserId(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoanResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoan(id, currentUserId()));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LoanResponse>> myLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(loanService.listMyLoans(currentUserId(), pageable));
    }

    @GetMapping("/{id}/schedule")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LoanPaymentResponse>> schedule(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getSchedule(id, currentUserId()));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoanPaymentResponse> pay(@PathVariable Long id,
                                                    @Valid @RequestBody LoanPaymentRequest request) {
        return ResponseEntity.ok(loanService.makePayment(id, request, currentUserId()));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.approveLoan(id, currentEmail()));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> reject(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(loanService.rejectLoan(id, reason));
    }

    @PatchMapping("/{id}/disburse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> disburse(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.disburseLoan(id));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<LoanResponse>> adminList(
            @RequestParam(required = false) Loan.LoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(loanService.listLoansByStatus(status, pageable));
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Long currentUserId() {
        return userRepository.findByEmail(currentEmail())
                .map(user -> user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
