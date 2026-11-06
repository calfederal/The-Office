package com.webapp.bankingplatform.controller;

import com.webapp.bankingplatform.dto.AchBatchRequest;
import com.webapp.bankingplatform.dto.AchFileResponse;
import com.webapp.bankingplatform.dto.AchTransferRequest;
import com.webapp.bankingplatform.dto.AchTransferResponse;
import com.webapp.bankingplatform.entity.AchFile;
import com.webapp.bankingplatform.entity.AchTransfer;
import com.webapp.bankingplatform.service.AchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ach")
public class AchController {

    private final AchService achService;

    public AchController(AchService achService) {
        this.achService = achService;
    }

    /** Create a single ACH transfer (credit or debit). */
    @PostMapping("/transfers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AchTransferResponse> createTransfer(@Valid @RequestBody AchTransferRequest req) {
        AchTransfer t = achService.createSingle(req);
        return ResponseEntity.ok(AchTransferResponse.from(t));
    }

    /** Create a batch ACH file from multiple transfers. */
    @PostMapping("/batches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchFileResponse> createBatch(@Valid @RequestBody AchBatchRequest req) {
        AchFile f = achService.createBatch(req);
        return ResponseEntity.ok(AchFileResponse.from(f));
    }

    /** Get a single transfer by id. */
    @GetMapping("/transfers/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AchTransferResponse> getTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(AchTransferResponse.from(achService.getById(id)));
    }

    /** Lookup by trace number. */
    @GetMapping("/trace/{traceNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AchTransferResponse> getByTrace(@PathVariable String traceNumber) {
        return ResponseEntity.ok(AchTransferResponse.from(achService.getByTraceNumber(traceNumber)));
    }

    /** List transfers, optionally filtered by status. */
    @GetMapping("/transfers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AchTransferResponse>> listTransfers(
            @RequestParam(required = false) String status) {
        List<AchTransferResponse> out = achService.listTransfers(status).stream()
            .map(AchTransferResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    /** Get a file by id (includes generated NACHA content). */
    @GetMapping("/files/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchFileResponse> getFile(@PathVariable Long id) {
        return ResponseEntity.ok(AchFileResponse.from(achService.getFileById(id)));
    }

    /** List all files. */
    @GetMapping("/files")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AchFileResponse>> listFiles() {
        List<AchFileResponse> out = achService.listFiles().stream()
            .map(AchFileResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    /** Mark a file as submitted to the ODFI. */
    @PostMapping("/files/{id}/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchFileResponse> submitFile(@PathVariable Long id) {
        return ResponseEntity.ok(AchFileResponse.from(achService.submitFile(id)));
    }

    /** Download the NACHA file content as plain text. */
    @GetMapping("/files/{id}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> downloadFile(@PathVariable Long id) {
        String content = achService.generateFileContent(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"ach-file-" + id + ".txt\"");
        return ResponseEntity.ok().headers(headers).body(content);
    }

    /** Process an inbound return (R01-R39). */
    @PostMapping("/returns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchTransferResponse> processReturn(
            @RequestParam String traceNumber,
            @RequestParam String returnCode) {
        return ResponseEntity.ok(AchTransferResponse.from(
            achService.processReturn(traceNumber, returnCode)));
    }
}
