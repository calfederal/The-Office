package com.webapp.bankingplatform.controller;

import com.webapp.bankingplatform.dto.BeneficiaryRequest;
import com.webapp.bankingplatform.dto.BeneficiaryResponse;
import com.webapp.bankingplatform.entity.Beneficiary;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.UserRepository;
import com.webapp.bankingplatform.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final UserRepository userRepository;

    private User currentUser() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(principal)
                .or(() -> userRepository.findByPhoneNumber(principal))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> create(@Valid @RequestBody BeneficiaryRequest req) {
        Beneficiary b = beneficiaryService.create(currentUser(), req);
        return ResponseEntity.ok(BeneficiaryResponse.from(b));
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> list() {
        List<BeneficiaryResponse> out = beneficiaryService.list(currentUser())
                .stream().map(BeneficiaryResponse::from).toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(BeneficiaryResponse.from(beneficiaryService.get(currentUser(), id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody BeneficiaryRequest req) {
        return ResponseEntity.ok(BeneficiaryResponse.from(beneficiaryService.update(currentUser(), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        beneficiaryService.delete(currentUser(), id);
        return ResponseEntity.noContent().build();
    }
}
