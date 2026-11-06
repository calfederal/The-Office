package com.webapp.bankingplatform.dto;

import com.webapp.bankingplatform.entity.FixedDeposit;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class FdRequest {

    @NotNull
    private FixedDeposit.FdType fdType;

    @NotNull
    @DecimalMin(value = "100.00")
    private BigDecimal principalAmount;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "20.00")
    private BigDecimal interestRate;

    @NotNull
    @Min(1)
    @Max(120)
    private Integer termMonths;

    @NotNull
    private FixedDeposit.InterestPayout interestPayout;

    private Boolean autoRenew = false;

    @Size(max = 200)
    private String nomineeName;

    @Size(max = 50)
    private String nomineeRelationship;

    @NotNull
    private Long sourceAccountId;

    private Long payoutAccountId;
}
