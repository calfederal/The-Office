package com.webapp.bankingplatform.dto;

import com.webapp.bankingplatform.entity.LoanPayment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanPaymentResponse {
    private Long id;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private BigDecimal paymentAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal totalDue;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal lateFee;
    private BigDecimal totalPaid;
    private BigDecimal outstandingAfter;
    private LoanPayment.PaymentStatus status;
    private LoanPayment.PaymentType paymentType;
    private String paymentMethod;
    private String transactionReference;
    private Long loanId;
    private Long accountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LoanPaymentResponse from(LoanPayment p) {
        return LoanPaymentResponse.builder()
                .id(p.getId())
                .installmentNumber(p.getInstallmentNumber())
                .dueDate(p.getDueDate())
                .paidDate(p.getPaidDate())
                .paymentAmount(p.getPaymentAmount())
                .principalAmount(p.getPrincipalAmount())
                .interestAmount(p.getInterestAmount())
                .principalDue(p.getPrincipalDue())
                .interestDue(p.getInterestDue())
                .totalDue(p.getTotalDue())
                .principalPaid(p.getPrincipalPaid())
                .interestPaid(p.getInterestPaid())
                .lateFee(p.getLateFee())
                .totalPaid(p.getTotalPaid())
                .outstandingAfter(p.getOutstandingAfter())
                .status(p.getStatus())
                .paymentType(p.getPaymentType())
                .paymentMethod(p.getPaymentMethod())
                .transactionReference(p.getTransactionReference())
                .loanId(p.getLoan() != null ? p.getLoan().getId() : null)
                .accountId(p.getAccount() != null ? p.getAccount().getId() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
