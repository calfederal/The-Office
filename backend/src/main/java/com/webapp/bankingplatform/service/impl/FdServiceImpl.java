package com.webapp.bankingplatform.service.impl;

import com.webapp.bankingplatform.dto.FdApplicationRequest;
import com.webapp.bankingplatform.dto.FdResponse;
import com.webapp.bankingplatform.entity.Account;
import com.webapp.bankingplatform.entity.FixedDeposit;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.exception.BankingException;
import com.webapp.bankingplatform.repository.AccountRepository;
import com.webapp.bankingplatform.repository.FixedDepositRepository;
import com.webapp.bankingplatform.repository.UserRepository;
import com.webapp.bankingplatform.service.FdService;
import com.webapp.bankingplatform.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class FdServiceImpl implements FdService {

    private final FixedDepositRepository fdRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FdServiceImpl(FixedDepositRepository fdRepository,
                         AccountRepository accountRepository,
                         UserRepository userRepository,
                         NotificationService notificationService) {
        this.fdRepository = fdRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public FdResponse openFd(Long userId, FdApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankingException("User not found"));

        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new BankingException("Source account not found"));
        if (!sourceAccount.getUser().getId().equals(userId)) {
            throw new BankingException("Source account does not belong to user");
        }
        if (sourceAccount.getBalance() < request.getPrincipalAmount().doubleValue()) {
            throw new BankingException("Insufficient balance to open FD");
        }

        Account payoutAccount = request.getPayoutAccountId() != null
                ? accountRepository.findById(request.getPayoutAccountId())
                        .orElseThrow(() -> new BankingException("Payout account not found"))
                : sourceAccount;

        // Debit source account
        sourceAccount.setBalance(sourceAccount.getBalance() - request.getPrincipalAmount().doubleValue());
        accountRepository.save(sourceAccount);

        FixedDeposit fd = new FixedDeposit();
        fd.setFdNumber(generateFdNumber());
        fd.setFdType(request.getFdType());
        fd.setPrincipalAmount(request.getPrincipalAmount());
        fd.setInterestRate(request.getInterestRate());
        fd.setTermMonths(request.getTermMonths());
        fd.setInterestPayout(request.getInterestPayout());
        fd.setOpeningDate(LocalDate.now());
        fd.setMaturityDate(LocalDate.now().plusMonths(request.getTermMonths()));
        fd.setStatus(FixedDeposit.FdStatus.ACTIVE);
        fd.setUser(user);
        fd.setSourceAccount(sourceAccount);
        fd.setPayoutAccount(payoutAccount);
        fd.setNomineeName(request.getNomineeName());
        fd.setNomineeRelationship(request.getNomineeRelationship());
        fd.setBranchCode(request.getBranchCode());
        fd.setAutoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : false);
        fd.setPrematureClosureAllowed(request.getPrematureClosureAllowed() != null
                ? request.getPrematureClosureAllowed() : true);

        BigDecimal maturity = computeMaturityAmount(
                request.getPrincipalAmount(),
                request.getInterestRate(),
                request.getTermMonths(),
                request.getFdType());
        fd.setMaturityAmount(maturity);

        FixedDeposit saved = fdRepository.save(fd);
        notificationService.sendNotification(userId,
                "FD opened: " + saved.getFdNumber() + " - matures " + saved.getMaturityDate(),
                "FD_OPENED");
        return FdResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FdResponse getFd(Long fdId) {
        FixedDeposit fd = fdRepository.findById(fdId)
                .orElseThrow(() -> new BankingException("FD not found"));
        return FdResponse.from(fd);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FdResponse> listMyFds(Long userId, Pageable pageable) {
        return fdRepository.findByUserId(userId, pageable).map(FdResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FdResponse> listFdsByStatus(FixedDeposit.FdStatus status, Pageable pageable) {
        return fdRepository.findByStatus(status, pageable).map(FdResponse::from);
    }

    @Override
    @Transactional
    public FdResponse closeFdPremature(Long fdId) {
        FixedDeposit fd = fdRepository.findById(fdId)
                .orElseThrow(() -> new BankingException("FD not found"));
        if (fd.getStatus() != FixedDeposit.FdStatus.ACTIVE) {
            throw new BankingException("FD is not active");
        }
        if (!fd.getPrematureClosureAllowed()) {
            throw new BankingException("Premature closure not allowed for this FD");
        }

        // Penalty: 1% of principal
        BigDecimal penalty = fd.getPrincipalAmount()
                .multiply(BigDecimal.valueOf(0.01))
                .setScale(2, RoundingMode.HALF_UP);
        fd.setPenaltyAmount(penalty);

        // Payout = principal + accrued interest - penalty
        BigDecimal payout = fd.getPrincipalAmount()
                .add(fd.getAccruedInterest() != null ? fd.getAccruedInterest() : BigDecimal.ZERO)
                .subtract(penalty);

        Account payoutAccount = fd.getPayoutAccount();
        payoutAccount.setBalance(payoutAccount.getBalance() + payout.doubleValue());
        accountRepository.save(payoutAccount);

        fd.setStatus(FixedDeposit.FdStatus.CLOSED);
        fd.setClosureDate(LocalDate.now());
        FixedDeposit saved = fdRepository.save(fd);
        notificationService.sendNotification(saved.getUser().getId(),
                "FD closed prematurely: " + saved.getFdNumber() + " - payout " + payout,
                "FD_CLOSED");
        return FdResponse.from(saved);
    }

    @Override
    @Transactional
    public FdResponse renewFd(Long fdId) {
        FixedDeposit old = fdRepository.findById(fdId)
                .orElseThrow(() -> new BankingException("FD not found"));
        if (old.getStatus() != FixedDeposit.FdStatus.MATURED) {
            throw new BankingException("Only matured FDs can be renewed");
        }

        FixedDeposit renewed = new FixedDeposit();
        renewed.setFdNumber(generateFdNumber());
        renewed.setFdType(old.getFdType());
        renewed.setPrincipalAmount(old.getMaturityAmount());
        renewed.setInterestRate(old.getInterestRate());
        renewed.setTermMonths(old.getTermMonths());
        renewed.setInterestPayout(old.getInterestPayout());
        renewed.setOpeningDate(LocalDate.now());
        renewed.setMaturityDate(LocalDate.now().plusMonths(old.getTermMonths()));
        renewed.setStatus(FixedDeposit.FdStatus.ACTIVE);
        renewed.setUser(old.getUser());
        renewed.setSourceAccount(old.getPayoutAccount());
        renewed.setPayoutAccount(old.getPayoutAccount());
        renewed.setAutoRenew(old.getAutoRenew());
        renewed.setPrematureClosureAllowed(old.getPrematureClosureAllowed());
        renewed.setMaturityAmount(computeMaturityAmount(
                renewed.getPrincipalAmount(),
                renewed.getInterestRate(),
                renewed.getTermMonths(),
                renewed.getFdType()));

        old.setStatus(FixedDeposit.FdStatus.RENEWED);
        fdRepository.save(old);
        FixedDeposit saved = fdRepository.save(renewed);
        return FdResponse.from(saved);
    }

    @Override
    @Transactional
    public int processMaturedFds() {
        List<FixedDeposit> matured = fdRepository.findMaturedAsOf(LocalDate.now());
        for (FixedDeposit fd : matured) {
            Account payout = fd.getPayoutAccount();
            payout.setBalance(payout.getBalance() + fd.getMaturityAmount().doubleValue());
            accountRepository.save(payout);
            fd.setStatus(FixedDeposit.FdStatus.MATURED);
            notificationService.sendNotification(fd.getUser().getId(),
                    "FD matured: " + fd.getFdNumber() + " - payout " + fd.getMaturityAmount(),
                    "FD_MATURED");
        }
        fdRepository.saveAll(matured);
        return matured.size();
    }

    // ---------- helpers ----------

    private String generateFdNumber() {
        long seq = fdRepository.maxId() + 1;
        return "FD" + String.format("%010d", seq);
    }

    private BigDecimal computeMaturityAmount(BigDecimal principal, BigDecimal rate,
                                             int months, FixedDeposit.FdType type) {
        // Quarterly compounding (standard for most FDs)
        BigDecimal annualRate = rate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal quarterlyRate = annualRate.divide(BigDecimal.valueOf(4), 10, RoundingMode.HALF_UP);
        int quarters = months / 3;
        BigDecimal factor = BigDecimal.ONE.add(quarterlyRate).pow(quarters);
        BigDecimal maturity = principal.multiply(factor).setScale(2, RoundingMode.HALF_UP);

        // Senior citizen bonus: +0.5%
        if (type == FixedDeposit.FdType.SENIOR_CITIZEN) {
            BigDecimal bonus = maturity.multiply(BigDecimal.valueOf(0.005))
                    .setScale(2, RoundingMode.HALF_UP);
            maturity = maturity.add(bonus);
        }
        return maturity;
    }
}
