package com.webapp.bankingplatform.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Data
public class DebitCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cardNumber;

    private String cardholderName;
    private String cvv;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String cardType; // STANDARD, GOLD, PLATINUM
    private String status; // ACTIVE, BLOCKED, EXPIRED, CANCELLED

    private Double dailyLimit;
    private Double monthlyLimit;

    private Boolean internationalEnabled = false;
    private Boolean onlineEnabled = true;
    private Boolean contactlessEnabled = true;

    // PIN stored as BCrypt hash, never plaintext
    private String pin;
    private boolean pinSet;
    private int failedPinAttempts;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
