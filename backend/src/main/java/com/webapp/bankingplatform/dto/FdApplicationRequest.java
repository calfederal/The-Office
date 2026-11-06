package com.webapp.bankingplatform.dto;

import com.webapp.bankingplatform.entity.FixedDeposit;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FdApplicationRequest {

    @NotNull
    private FixedDeposit.FdType fdType;

    @NotNull
    @DecimalMin(value = "100.00", message = "Minimum FD amount is 100")
    @DecimalMax(value = "10000000.00", message = "Maximum FD amount is 10,000,000")
    private BigDecimal principalAmount;

    @NotNull
    @DecimalMin(value = "0.01", message = "Interest rate must be positive")
    @DecimalMax(value = "20.00", message = "Interest rate cannot exceed 20%")
    private BigDecimal interestRate;

    @NotNull
    @Min(value = 1, message = "Term must be at least 1 month")
    @Max(value = 240, message = "Term cannot exceed 240 months (20 years)")
    private Integer termMonths;

    @NotNull
    private FixedDeposit.InterestPayout interestPayout;

    @NotNull
    private Long sourceAccountId;

    private Long payoutAccountId; // defaults to source

    @Size(max = 200)
    private String nomineeName;

    @Size(max = 50)
    private String nomineeRelationship;

    @Size(max = 100)
    private String branchCode;

    private Boolean autoRenew;
    private Boolean prematureClosureAllowed;
}
