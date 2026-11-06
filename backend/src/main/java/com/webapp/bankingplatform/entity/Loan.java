package com.webapp.bankingplatform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans", indexes = {
    @Index(name = "idx_loan_number", columnList = "loanNumber", unique = true),
    @Index(name = "idx_loan_user", columnList = "user_id"),
    @Index(name = "idx_loan_status", columnList = "status")
})
@Data
public class Loan {

    public enum LoanType { PERSONAL, HOME, MORTGAGE, AUTO, EDUCATION, BUSINESS, LINE_OF_CREDIT }
    public enum LoanStatus { PENDING, APPROVED, ACTIVE, CLOSED, REJECTED, DEFAULTED, RESTRUCTURED }
    public enum InterestType { FIXED, VARIABLE, FLOATING }
    public enum RepaymentFrequency { MONTHLY, BIWEEKLY, WEEKLY, QUARTERLY }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String loanNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanType loanType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterestType interestType;

    @Column(nullable = false)
    private Integer termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepaymentFrequency repaymentFrequency;

    // EMI / monthly payment amount
    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyPayment;

    // Outstanding balance (what the borrower still owes)
    @Column(precision = 19, scale = 2)
    private BigDecimal outstandingBalance;

    // Total amount paid to date
    @Column(precision = 19, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    // Total interest paid to date
    @Column(precision = 19, scale = 2)
    private BigDecimal totalInterest = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate applicationDate;

    private LocalDate approvalDate;
    private LocalDate disbursementDate;
    private LocalDate firstPaymentDate;
    private LocalDate maturityDate;
    private LocalDate nextPaymentDate;
    private LocalDate lastPaymentDate;
    private LocalDate closedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(length = 500)
    private String purpose;

    @Column(length = 100)
    private String collateralDescription;

    @Column(precision = 19, scale = 2)
    private BigDecimal collateralValue;

    // Credit score at time of application (for risk grading)
    @Column
    private Integer creditScoreAtApplication;

    // Risk grade: A+, A, B, C, D, E
    @Column(length = 5)
    private String riskGrade;

    @Column(nullable = false)
    private Integer remainingPayments;

    @Column(nullable = false)
    private Integer missedPayments = 0;

    @Column(precision = 19, scale = 2)
    private BigDecimal lateFeeAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 100)
    private String approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The loan's own account (for tracking)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // The account that receives the disbursement funds
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursement_account_id")
    private Account disbursementAccount;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanPayment> payments = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.applicationDate == null) this.applicationDate = LocalDate.now();
        if (this.status == null) this.status = LoanStatus.PENDING;
        if (this.missedPayments == null) this.missedPayments = 0;
        if (this.lateFeeAmount == null) this.lateFeeAmount = BigDecimal.ZERO;
        if (this.totalPaid == null) this.totalPaid = BigDecimal.ZERO;
        if (this.totalInterest == null) this.totalInterest = BigDecimal.ZERO;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
