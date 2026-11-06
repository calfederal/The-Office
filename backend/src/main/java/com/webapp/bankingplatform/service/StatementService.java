package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.dto.StatementDto;
import com.webapp.bankingplatform.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface StatementService {
    StatementDto generateStatement(Long accountId, LocalDate from, LocalDate to);
    byte[] generatePdf(Long accountId, LocalDate from, LocalDate to);
    String generateCsv(Long accountId, LocalDate from, LocalDate to);
    void emailStatement(User user, Long accountId, LocalDate from, LocalDate to);
    List<String> availablePeriods(Long accountId);
}
