package com.webapp.bankingplatform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fixed_deposits", indexes = {
    @Index(name = "idx_fd_number", columnList = "fdNumber", unique = true),
    @Index(name = "idx_fd_user", columnList = "user_id"),
    @Index(name = "idx_fd_status", columnList = "status")
})
public class FixedDeposit {

    public enum FdStatus { ACTIVE, MATURED, CLOSED, RENEWED, BROKEN }
    public enum InterestPayout { AT_MATURITY, MONTHLY, QUARTERLY, ANNUALLY }
    public enum FdType { REGULAR, SENIOR_CITIZEN, TAX_SAVER, FLEXI, CORPORATE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String fdNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FdType fdType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterestPayout interestPayout;

    @Column(nullable = false)
    private LocalDate openingDate;

    private LocalDate maturityDate;

    @Column(precision = 19, scale = 2)
    private BigDecimal maturityAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal interestPaidOut = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FdStatus status;

    @Column(nullable = false)
    private Boolean autoRenew = false;

    @Column(nullable = false)
    private Boolean prematureClosureAllowed = true;

    @Column(precision = 19, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(length = 200)
    private String nomineeName;

    @Column(length = 50)
    private String nomineeRelationship;

    @Column(length = 100)
    private String branchCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_account_id")
    private Account payoutAccount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = FdStatus.ACTIVE;
        if (this.accruedInterest == null) this.accruedInterest = BigDecimal.ZERO;
        if (this.interestPaidOut == null) this.interestPaidOut = BigDecimal.ZERO;
        if (this.penaltyAmount == null) this.penaltyAmount = BigDecimal.ZERO;
        if (this.autoRenew == null) this.autoRenew = false;
        if (this.prematureClosureAllowed == null) this.prematureClosureAllowed = true;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
