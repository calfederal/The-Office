package com.webapp.bankingplatform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_payments", indexes = {
    @Index(name = "idx_lp_loan", columnList = "loan_id"),
    @Index(name = "idx_lp_due", columnList = "dueDate"),
    @Index(name = "idx_lp_status", columnList = "status")
})
@Data
public class LoanPayment {

    public enum PaymentStatus { SCHEDULED, DUE, PAID, OVERDUE, PARTIAL, WAIVED, WRITTEN_OFF }
    public enum PaymentType { EMI, PRINCIPAL_ONLY, INTEREST_ONLY, LATE_FEE, PENALTY, PREPAYMENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate paidDate;

    // Total payment amount (principal + interest + fees)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    // Principal portion of this installment
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    // Interest portion of this installment
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal interestAmount;

    // Detailed breakdown (kept for audit/reporting)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalDue;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal interestDue;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDue;

    @Column(precision = 19, scale = 2)
    private BigDecimal principalPaid = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal interestPaid = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal outstandingAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType paymentType;

    // How the payment was made (ACH, WIRE, INTERNAL_TRANSFER, etc.)
    @Column(length = 50)
    private String paymentMethod;

    @Column(length = 200)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = PaymentStatus.SCHEDULED;
        if (this.paymentType == null) this.paymentType = PaymentType.EMI;
        if (this.principalPaid == null) this.principalPaid = BigDecimal.ZERO;
        if (this.interestPaid == null) this.interestPaid = BigDecimal.ZERO;
        if (this.lateFee == null) this.lateFee = BigDecimal.ZERO;
        if (this.totalPaid == null) this.totalPaid = BigDecimal.ZERO;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
