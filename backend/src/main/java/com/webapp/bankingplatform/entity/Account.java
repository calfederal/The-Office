package com.webapp.bankingplatform.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    private String accountType; // SAVINGS, CHECKING, etc.
    private Double balance;
    private String branch;
    private String ifscCode;

    // PIN stored as BCrypt hash, never plaintext
    private String pin;
    private boolean pinCreated;

    // Admin can freeze an account to block all transactions
    @Column(nullable = false)
    private boolean frozen = false;

    // Optimistic locking to prevent lost updates on concurrent balance changes
    @Version
    private Long version;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "sourceAccount", cascade = CascadeType.ALL)
    private java.util.List<Transaction> sourceTransactions;

    @OneToMany(mappedBy = "targetAccount", cascade = CascadeType.ALL)
    private java.util.List<Transaction> targetTransactions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
