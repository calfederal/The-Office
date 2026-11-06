package com.webapp.bankingplatform.dto;

import com.webapp.bankingplatform.entity.Loan;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class LoanRequest {
    @NotNull private Loan.LoanType loanType;
    @NotNull @DecimalMin("100.00") private BigDecimal principalAmount;
    @NotNull @DecimalMin("0.01") @DecimalMax("50.00") private BigDecimal interestRate;
    private Loan.InterestType interestType = Loan.InterestType.REDUCING_BALANCE;
    @NotNull @Min(1) @Max(480) private Integer termMonths;
    private Loan.RepaymentFrequency repaymentFrequency = Loan.RepaymentFrequency.MONTHLY;
    @NotBlank @Size(max = 500) private String purpose;
    @Size(max = 1000) private String collateralDescription;
    private BigDecimal collateralValue;
    private Long disbursementAccountId;
}
