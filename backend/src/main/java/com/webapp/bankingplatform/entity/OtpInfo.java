package com.webapp.bankingplatform.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class OtpInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    private String otp;
    private LocalDateTime expiryDate;
    private String purpose; // LOGIN, PASSWORD_RESET

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
