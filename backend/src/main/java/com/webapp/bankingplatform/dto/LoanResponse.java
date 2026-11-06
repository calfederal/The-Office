package com.webapp.bankingplatform.dto;

import com.webapp.bankingplatform.entity.Loan;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanResponse {
    private Long id;
    private String loanNumber;
    private Loan.LoanType loanType;
    private Loan.LoanStatus status;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Loan.InterestType interestType;
    private Integer termMonths;
    private Loan.RepaymentFrequency repaymentFrequency;
    private BigDecimal monthlyPayment;
    private BigDecimal outstandingBalance;
    private BigDecimal totalPaid;
    private BigDecimal totalInterest;
    private LocalDate applicationDate;
    private LocalDate approvalDate;
    private LocalDate disbursementDate;
    private LocalDate firstPaymentDate;
    private LocalDate maturityDate;
    private LocalDate nextPaymentDate;
    private LocalDate lastPaymentDate;
    private LocalDate closedDate;
    private String purpose;
    private String collateralDescription;
    private BigDecimal collateralValue;
    private Integer creditScoreAtApplication;
    private String riskGrade;
    private Integer remainingPayments;
    private Integer missedPayments;
    private BigDecimal lateFeeAmount;
    private String rejectionReason;
    private String approvedBy;
    private Long userId;
    private String userName;
    private Long accountId;
    private Long disbursementAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LoanResponse from(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .loanNumber(loan.getLoanNumber())
                .loanType(loan.getLoanType())
                .status(loan.getStatus())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .interestType(loan.getInterestType())
                .termMonths(loan.getTermMonths())
                .repaymentFrequency(loan.getRepaymentFrequency())
                .monthlyPayment(loan.getMonthlyPayment())
                .outstandingBalance(loan.getOutstandingBalance())
                .totalPaid(loan.getTotalPaid())
                .totalInterest(loan.getTotalInterest())
                .applicationDate(loan.getApplicationDate())
                .approvalDate(loan.getApprovalDate())
                .disbursementDate(loan.getDisbursementDate())
                .firstPaymentDate(loan.getFirstPaymentDate())
                .maturityDate(loan.getMaturityDate())
                .nextPaymentDate(loan.getNextPaymentDate())
                .lastPaymentDate(loan.getLastPaymentDate())
                .closedDate(loan.getClosedDate())
                .purpose(loan.getPurpose())
                .collateralDescription(loan.getCollateralDescription())
                .collateralValue(loan.getCollateralValue())
                .creditScoreAtApplication(loan.getCreditScoreAtApplication())
                .riskGrade(loan.getRiskGrade())
                .remainingPayments(loan.getRemainingPayments())
                .missedPayments(loan.getMissedPayments())
                .lateFeeAmount(loan.getLateFeeAmount())
                .rejectionReason(loan.getRejectionReason())
                .approvedBy(loan.getApprovedBy())
                .userId(loan.getUser() != null ? loan.getUser().getId() : null)
                .userName(loan.getUser() != null ? loan.getUser().getName() : null)
                .accountId(loan.getAccount() != null ? loan.getAccount().getId() : null)
                .disbursementAccountId(loan.getDisbursementAccount() != null ? loan.getDisbursementAccount().getId() : null)
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}
