package com.webapp.bankingplatform.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER, FEE, INTEREST, REVERSAL
    private Double amount;
    private String currency = "USD";
    private String status; // PENDING, COMPLETED, FAILED, REVERSED
    private String description;
    private String referenceNumber;
    private LocalDateTime transactionDate;

    // For audit / reconciliation
    private Double balanceAfter;

    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void onCreate() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
        if (this.referenceNumber == null) {
            this.referenceNumber = "TXN-" + System.currentTimeMillis() + "-"
                    + Long.toHexString(Double.doubleToLongBits(Math.random())).toUpperCase();
        }
    }
}
