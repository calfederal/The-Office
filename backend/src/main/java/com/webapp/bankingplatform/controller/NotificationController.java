package com.webapp.bankingplatform.controller;

import com.webapp.bankingplatform.dto.NotificationPreferenceRequest;
import com.webapp.bankingplatform.dto.NotificationResponse;
import com.webapp.bankingplatform.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserId();
        Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(notificationService.list(userId, status, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(currentUserId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, Boolean>> markRead(@PathVariable Long id) {
        boolean ok = notificationService.markRead(id, currentUserId());
        return ResponseEntity.ok(Map.of("updated", ok));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead() {
        return ResponseEntity.ok(Map.of("updated", notificationService.markAllRead(currentUserId())));
    }

    @PutMapping("/preferences")
    public ResponseEntity<Void> savePreference(@Valid @RequestBody NotificationPreferenceRequest req) {
        notificationService.savePreference(currentUserId(), req);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(p.toString());
    }
}
