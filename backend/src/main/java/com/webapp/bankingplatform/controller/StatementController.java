package com.webapp.bankingplatform.controller;

import com.webapp.bankingplatform.dto.StatementDto;
import com.webapp.bankingplatform.entity.Account;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.AccountRepository;
import com.webapp.bankingplatform.repository.UserRepository;
import com.webapp.bankingplatform.service.StatementService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statements")
public class StatementController {

    private final StatementService statementService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public StatementController(StatementService statementService,
                               AccountRepository accountRepository,
                               UserRepository userRepository) {
        this.statementService = statementService;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StatementDto> getStatement(
            @PathVariable Long accountId,
            @RequestParam String from,
            @RequestParam String to) {
        User user = currentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().getId().equals(user.getId())
                && !user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new SecurityException("Not authorized for this account");
        }
        StatementDto statement = statementService.generateStatement(
                accountId, LocalDate.parse(from), LocalDate.parse(to));
        return ResponseEntity.ok(statement);
    }

    @GetMapping("/{accountId}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ByteArrayResource> downloadPdf(
            @PathVariable Long accountId,
            @RequestParam String from,
            @RequestParam String to) {
        User user = currentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().getId().equals(user.getId())
                && !user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new SecurityException("Not authorized for this account");
        }
        byte[] pdf = statementService.generatePdf(
                accountId, LocalDate.parse(from), LocalDate.parse(to));
        ByteArrayResource resource = new ByteArrayResource(pdf);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=statement-" + accountId + ".pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/{accountId}/csv")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> downloadCsv(
            @PathVariable Long accountId,
            @RequestParam String from,
            @RequestParam String to) {
        User user = currentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().getId().equals(user.getId())
                && !user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new SecurityException("Not authorized for this account");
        }
        String csv = statementService.generateCsv(
                accountId, LocalDate.parse(from), LocalDate.parse(to));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=statement-" + accountId + ".csv");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/{accountId}/email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> emailStatement(
            @PathVariable Long accountId,
            @RequestParam String from,
            @RequestParam String to) {
        User user = currentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().getId().equals(user.getId())
                && !user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new SecurityException("Not authorized for this account");
        }
        statementService.emailStatement(user, accountId,
                LocalDate.parse(from), LocalDate.parse(to));
        return ResponseEntity.ok("Statement emailed");
    }

    @GetMapping("/{accountId}/available-periods")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> availablePeriods(@PathVariable Long accountId) {
        User user = currentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().getId().equals(user.getId())
                && !user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new SecurityException("Not authorized for this account");
        }
        return ResponseEntity.ok(statementService.availablePeriods(accountId));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
