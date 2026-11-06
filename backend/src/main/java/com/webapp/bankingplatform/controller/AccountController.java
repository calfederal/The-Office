package com.webapp.bankingplatform.controller;

import com.webapp.bankingplatform.entity.Account;
import com.webapp.bankingplatform.entity.Transaction;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.security.PinEncoder;
import com.webapp.bankingplatform.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PinEncoder pinEncoder;

    @PostMapping("/create-pin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPin(@RequestBody Map<String, Object> request) {
        try {
            Long accountId = ((Number) request.get("accountId")).longValue();
            String pin = (String) request.get("pin");
            User caller = currentUser();
            Account account = accountService.createPin(caller, accountId, pin);
            return ok("PIN created successfully", account);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/check-pin-created")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkPinCreated(@RequestBody Map<String, Object> request) {
        try {
            Long accountId = ((Number) request.get("accountId")).longValue();
            User caller = currentUser();
            boolean isPinCreated = accountService.isPinCreated(caller, accountId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isPinCreated", isPinCreated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/update-pin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePin(@RequestBody Map<String, Object> request) {
        try {
            Long accountId = ((Number) request.get("accountId")).longValue();
            String oldPin = (String) request.get("oldPin");
            String newPin = (String) request.get("newPin");
            User caller = currentUser();
            Account account = accountService.updatePin(caller, accountId, oldPin, newPin);
            return ok("PIN updated successfully", account);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> withdraw(@RequestBody Map<String, Object> request) {
        try {
            Long accountId = ((Number) request.get("accountId")).longValue();
            Double amount = ((Number) request.get("amount")).doubleValue();
            User caller = currentUser();
            Account account = accountService.cashWithdrawal(caller, accountId, amount);
            return ok("Withdrawal successful", account);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/deposit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> request) {
        try {
            Long accountId = ((Number) request.get("accountId")).longValue();
            Double amount = ((Number) request.get("amount")).doubleValue();
            User caller = currentUser();
            Account account = accountService.cashDeposit(caller, accountId, amount);
            return ok("Deposit successful", account);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/fund-transfer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> fundTransfer(@RequestBody Map<String, Object> request) {
        try {
            Long sourceAccountId = ((Number) request.get("sourceAccountId")).longValue();
            Long targetAccountId = ((Number) request.get("targetAccountId")).longValue();
            Double amount = ((Number) request.get("amount")).doubleValue();
            User caller = currentUser();
            Account account = accountService.fundTransfer(caller, sourceAccountId, targetAccountId, amount);
            return ok("Transfer successful", account);
        } catch (Exception e) {
            return error(e);
        }
    }

    // FIX: was @GetMapping + @RequestBody (broken). Now @GetMapping + @RequestParam.
    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTransactions(@RequestParam Long accountId) {
        try {
            User caller = currentUser();
            List<Transaction> transactions = accountService.getTransactions(caller, accountId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(e);
        }
    }

    // FIX: was @GetMapping + @RequestBody (broken). Now @GetMapping + @RequestParam.
    @GetMapping("/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAccountDetails(@RequestParam Long accountId) {
        try {
            User caller = currentUser();
            Account account = accountService.getAccountDetails(caller, accountId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("account", account);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/validate-pin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> validatePin(@RequestBody Map<String, Object> request) {
        try {
            Long accountId = ((Number) request.get("accountId")).longValue();
            String pin = (String) request.get("pin");
            User caller = currentUser();
            Account account = accountService.validatePin(caller, accountId, pin);
            return ok("PIN validated successfully", account);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/validate-amount")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> validateAmount(@RequestBody Map<String, Object> request) {
        try {
            Double amount = ((Number) request.get("amount")).doubleValue();
            accountService.validateAmount(amount);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Amount validated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/send-statement")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendStatement(@RequestBody Map<String, Object> request) {
        try {
            Long accountId = ((Number) request.get("accountId")).longValue();
            String email = (String) request.get("email");
            User caller = currentUser();
            accountService.sendStatement(caller, accountId, email);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Statement sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(e);
        }
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User u)) {
            throw new RuntimeException("Not authenticated");
        }
        return u;
    }

    private ResponseEntity<Map<String, Object>> ok(String message, Account account) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("account", account);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> error(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
