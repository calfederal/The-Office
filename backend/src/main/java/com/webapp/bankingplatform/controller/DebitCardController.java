package com.webapp.bankingplatform.controller;

import com.webapp.bankingplatform.entity.DebitCard;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.service.DebitCardService;
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
@RequestMapping("/api/debit-card")
@CrossOrigin(origins = "*")
public class DebitCardController {

    @Autowired
    private DebitCardService debitCardService;

    @PostMapping("/issue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> issueCard(@RequestBody Map<String, Object> request) {
        try {
            Long accountId = ((Number) request.get("accountId")).longValue();
            String cardType = (String) request.getOrDefault("cardType", "STANDARD");
            User caller = currentUser();
            DebitCard card = debitCardService.issueCard(caller, accountId, cardType);
            return ok("Card issued successfully", card);
        } catch (Exception e) {
            return error(e);
        }
    }

    @GetMapping("/{cardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCard(@PathVariable Long cardId) {
        try {
            User caller = currentUser();
            DebitCard card = debitCardService.getCardById(caller, cardId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("card", card);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(e);
        }
    }

    @GetMapping("/by-account/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCardsByAccount(@PathVariable Long accountId) {
        try {
            User caller = currentUser();
            List<DebitCard> cards = debitCardService.getCardsByAccount(caller, accountId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cards", cards);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/change-pin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePin(@RequestBody Map<String, Object> request) {
        try {
            Long cardId = ((Number) request.get("cardId")).longValue();
            String oldPin = (String) request.get("oldPin");
            String newPin = (String) request.get("newPin");
            User caller = currentUser();
            DebitCard card = debitCardService.changePin(caller, cardId, oldPin, newPin);
            return ok("PIN changed successfully", card);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/validate-pin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> validatePin(@RequestBody Map<String, Object> request) {
        try {
            Long cardId = ((Number) request.get("cardId")).longValue();
            String pin = (String) request.get("pin");
            User caller = currentUser();
            boolean valid = debitCardService.validatePin(caller, cardId, pin);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", valid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/block")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> blockCard(@RequestBody Map<String, Object> request) {
        try {
            Long cardId = ((Number) request.get("cardId")).longValue();
            String reason = (String) request.getOrDefault("reason", "USER_REQUEST");
            User caller = currentUser();
            DebitCard card = debitCardService.blockCard(caller, cardId, reason);
            return ok("Card blocked", card);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/report-lost")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> reportLostOrStolen(@RequestBody Map<String, Object> request) {
        try {
            Long cardId = ((Number) request.get("cardId")).longValue();
            String reason = (String) request.getOrDefault("reason", "LOST");
            User caller = currentUser();
            DebitCard card = debitCardService.reportLostOrStolen(caller, cardId, reason);
            return ok("Card reported and blocked", card);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/toggle")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleFeature(@RequestBody Map<String, Object> request) {
        try {
            Long cardId = ((Number) request.get("cardId")).longValue();
            String feature = (String) request.get("feature"); // international, online, contactless
            Boolean enabled = (Boolean) request.get("enabled");
            User caller = currentUser();
            DebitCard card = debitCardService.toggleFeature(caller, cardId, feature, enabled);
            return ok("Feature updated", card);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/update-limits")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateLimits(@RequestBody Map<String, Object> request) {
        try {
            Long cardId = ((Number) request.get("cardId")).longValue();
            Double dailyLimit = request.get("dailyLimit") != null
                    ? ((Number) request.get("dailyLimit")).doubleValue() : null;
            Double monthlyLimit = request.get("monthlyLimit") != null
                    ? ((Number) request.get("monthlyLimit")).doubleValue() : null;
            User caller = currentUser();
            DebitCard card = debitCardService.updateLimits(caller, cardId, dailyLimit, monthlyLimit);
            return ok("Limits updated", card);
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

    private ResponseEntity<Map<String, Object>> ok(String message, DebitCard card) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("card", card);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> error(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
