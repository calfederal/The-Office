package com.bankingplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries", indexes = {
        @Index(name = "idx_beneficiary_owner", columnList = "owner_id"),
        @Index(name = "idx_beneficiary_nickname", columnList = "owner_id,nickname")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 64)
    private String nickname;

    @Column(nullable = false, length = 128)
    private String beneficiaryName;

    @Column(length = 64)
    private String bankName;

    /** ABA routing number (9 digits) for ACH, or SWIFT/BIC for wires. */
    @Column(length = 16)
    private String routingNumber;

    @Column(nullable = false, length = 32)
    private String accountNumber;

    /** SAVINGS / CHECKING / WALLET / WIRE / INTERNATIONAL */
    @Column(length = 16)
    private String accountType;

    /** DOMESTIC / INTERNATIONAL */
    @Column(length = 16)
    private String transferType;

    /** Optional address for wires. */
    @Column(length = 128)
    private String addressLine1;

    @Column(length = 64)
    private String city;

    @Column(length = 64)
    private String state;

    @Column(length = 16)
    private String postalCode;

    @Column(length = 2)
    private String countryCode;

    @Column(length = 64)
    private String phoneNumber;

    @Column(length = 128)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.accountType == null) this.accountType = "CHECKING";
        if (this.transferType == null) this.transferType = "DOMESTIC";
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
