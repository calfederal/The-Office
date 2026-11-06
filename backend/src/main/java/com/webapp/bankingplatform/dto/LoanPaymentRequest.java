package com.webapp.bankingplatform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanPaymentRequest {

    @NotNull
    @Min(value = 1, message = "Installment number must be positive")
    private Integer installmentNumber;

    @NotNull
    @DecimalMin(value = "0.01", message = "Payment amount must be positive")
    private BigDecimal paymentAmount;

    @NotBlank
    @Size(max = 50)
    private String paymentMethod; // ACH, WIRE, INTERNAL_TRANSFER, CARD

    @Size(max = 200)
    private String transactionReference;

    // Optional: account to debit (defaults to user's primary account)
    private Long sourceAccountId;
}
