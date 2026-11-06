package com.webapp.bankingplatform.dto;

import com.webapp.bankingplatform.entity.Loan;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    @NotNull
    private Loan.LoanType loanType;

    @NotNull
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is 1,000")
    @DecimalMax(value = "10000000.00", message = "Maximum loan amount is 10,000,000")
    private BigDecimal principalAmount;

    @NotNull
    @DecimalMin(value = "0.01", message = "Interest rate must be positive")
    @DecimalMax(value = "50.00", message = "Interest rate cannot exceed 50%")
    private BigDecimal interestRate;

    @NotNull
    private Loan.InterestType interestType;

    @NotNull
    @Min(value = 1, message = "Term must be at least 1 month")
    @Max(value = 480, message = "Term cannot exceed 480 months (40 years)")
    private Integer termMonths;

    @NotNull
    private Loan.RepaymentFrequency repaymentFrequency;

    @Size(max = 500)
    private String purpose;

    @Size(max = 100)
    private String collateralDescription;

    @DecimalMin(value = "0.00")
    private BigDecimal collateralValue;

    @Min(300)
    @Max(850)
    private Integer creditScore;

    @NotNull
    private Long disbursementAccountId;
}
