package com.webapp.bankingplatform.dto;

import com.webapp.bankingplatform.entity.FixedDeposit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FdResponse {
    private Long id;
    private String fdNumber;
    private FixedDeposit.FdType fdType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private FixedDeposit.InterestPayout interestPayout;
    private LocalDate openingDate;
    private LocalDate maturityDate;
    private BigDecimal maturityAmount;
    private BigDecimal accruedInterest;
    private BigDecimal interestPaidOut;
    private FixedDeposit.FdStatus status;
    private Boolean autoRenew;
    private Boolean prematureClosureAllowed;
    private BigDecimal penaltyAmount;
    private String nomineeName;
    private String nomineeRelationship;
    private String branchCode;
    private Long userId;
    private String userName;
    private Long sourceAccountId;
    private Long payoutAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FdResponse from(FixedDeposit fd) {
        return FdResponse.builder()
                .id(fd.getId())
                .fdNumber(fd.getFdNumber())
                .fdType(fd.getFdType())
                .principalAmount(fd.getPrincipalAmount())
                .interestRate(fd.getInterestRate())
                .termMonths(fd.getTermMonths())
                .interestPayout(fd.getInterestPayout())
                .openingDate(fd.getOpeningDate())
                .maturityDate(fd.getMaturityDate())
                .maturityAmount(fd.getMaturityAmount())
                .accruedInterest(fd.getAccruedInterest())
                .interestPaidOut(fd.getInterestPaidOut())
                .status(fd.getStatus())
                .autoRenew(fd.getAutoRenew())
                .prematureClosureAllowed(fd.getPrematureClosureAllowed())
                .penaltyAmount(fd.getPenaltyAmount())
                .nomineeName(fd.getNomineeName())
                .nomineeRelationship(fd.getNomineeRelationship())
                .branchCode(fd.getBranchCode())
                .userId(fd.getUser() != null ? fd.getUser().getId() : null)
                .userName(fd.getUser() != null ? fd.getUser().getName() : null)
                .sourceAccountId(fd.getSourceAccount() != null ? fd.getSourceAccount().getId() : null)
                .payoutAccountId(fd.getPayoutAccount() != null ? fd.getPayoutAccount().getId() : null)
                .createdAt(fd.getCreatedAt())
                .updatedAt(fd.getUpdatedAt())
                .build();
    }
}
