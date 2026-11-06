package com.webapp.bankingplatform.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String password;
    private String email;
    private String countryCode;
    private String phoneNumber;
    private String address;
    private String accountNumber;

    private String role; // USER, ADMIN, AUDITOR

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean locked = false;

    // KYC status: PENDING, APPROVED, REJECTED, EXPIRED
    @Column(nullable = false)
    private String kycStatus = "PENDING";

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Account account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private java.util.List<Token> tokens;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private java.util.List<OtpInfo> otpInfos;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
