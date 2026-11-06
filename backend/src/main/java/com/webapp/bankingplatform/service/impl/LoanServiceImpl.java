package com.webapp.bankingplatform.service.impl;

import com.webapp.bankingplatform.dto.LoanApplicationRequest;
import com.webapp.bankingplatform.dto.LoanPaymentRequest;
import com.webapp.bankingplatform.dto.LoanPaymentResponse;
import com.webapp.bankingplatform.dto.LoanResponse;
import com.webapp.bankingplatform.entity.Account;
import com.webapp.bankingplatform.entity.Loan;
import com.webapp.bankingplatform.entity.LoanPayment;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.exception.BankingException;
import com.webapp.bankingplatform.repository.AccountRepository;
import com.webapp.bankingplatform.repository.LoanPaymentRepository;
import com.webapp.bankingplatform.repository.LoanRepository;
import com.webapp.bankingplatform.repository.UserRepository;
import com.webapp.bankingplatform.service.LoanService;
import com.webapp.bankingplatform.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LoanServiceImpl(LoanRepository loanRepository,
                           LoanPaymentRepository loanPaymentRepository,
                           AccountRepository accountRepository,
                           UserRepository userRepository,
                           NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.loanPaymentRepository = loanPaymentRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public LoanResponse applyForLoan(Long userId, LoanApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankingException("User not found"));

        Account disbursementAccount = accountRepository.findById(request.getDisbursementAccountId())
                .orElseThrow(() -> new BankingException("Disbursement account not found"));

        if (!disbursementAccount.getUser().getId().equals(userId)) {
            throw new BankingException("Disbursement account does not belong to user");
        }

        Loan loan = new Loan();
        loan.setLoanNumber(generateLoanNumber());
        loan.setLoanType(request.getLoanType());
        loan.setPrincipalAmount(request.getPrincipalAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setInterestType(request.getInterestType());
        loan.setTermMonths(request.getTermMonths());
        loan.setRepaymentFrequency(request.getRepaymentFrequency());
        loan.setPurpose(request.getPurpose());
        loan.setCollateralDescription(request.getCollateralDescription());
        loan.setCollateralValue(request.getCollateralValue());
        loan.setCreditScoreAtApplication(request.getCreditScore());
        loan.setRiskGrade(gradeRisk(request.getCreditScore()));
        loan.setStatus(Loan.LoanStatus.PENDING);
        loan.setUser(user);
        loan.setDisbursementAccount(disbursementAccount);
        loan.setRemainingPayments(request.getTermMonths());

        // Compute monthly payment (EMI) using standard amortization
        BigDecimal monthlyRate = request.getInterestRate()
                .divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal emi = computeEmi(request.getPrincipalAmount(), monthlyRate, request.getTermMonths());
        loan.setMonthlyPayment(emi);
        loan.setOutstandingBalance(request.getPrincipalAmount());

        Loan saved = loanRepository.save(loan);
        notificationService.sendNotification(userId,
                "Loan application received: " + saved.getLoanNumber(),
                "LOAN_APPLICATION");
        return LoanResponse.from(saved);
    }

    @Override
    @Transactional
    public LoanResponse approveLoan(Long loanId, String approvedBy) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BankingException("Loan not found"));
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new BankingException("Loan is not in PENDING status");
        }
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDate.now());
        loan.setApprovedBy(approvedBy);
        Loan saved = loanRepository.save(loan);
        notificationService.sendNotification(saved.getUser().getId(),
                "Loan approved: " + saved.getLoanNumber(),
                "LOAN_APPROVED");
        return LoanResponse.from(saved);
    }

    @Override
    @Transactional
    public LoanResponse rejectLoan(Long loanId, String reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BankingException("Loan not found"));
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new BankingException("Loan is not in PENDING status");
        }
        loan.setStatus(Loan.LoanStatus.REJECTED);
        loan.setRejectionReason(reason);
        Loan saved = loanRepository.save(loan);
        notificationService.sendNotification(saved.getUser().getId(),
                "Loan rejected: " + saved.getLoanNumber() + " - " + reason,
                "LOAN_REJECTED");
        return LoanResponse.from(saved);
    }

    @Override
    @Transactional
    public LoanResponse disburseLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BankingException("Loan not found"));
        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            throw new BankingException("Loan must be APPROVED before disbursement");
        }
        Account account = loan.getDisbursementAccount();
        if (account == null) {
            throw new BankingException("No disbursement account set");
        }
        account.setBalance(account.getBalance() + loan.getPrincipalAmount().doubleValue());
        accountRepository.save(account);

        loan.setStatus(Loan.LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());
        loan.setFirstPaymentDate(LocalDate.now().plusMonths(1));
        loan.setMaturityDate(LocalDate.now().plusMonths(loan.getTermMonths()));
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setAccount(account);

        // Generate amortization schedule
        generateSchedule(loan);
        Loan saved = loanRepository.save(loan);
        notificationService.sendNotification(saved.getUser().getId(),
                "Loan disbursed: " + saved.getLoanNumber() + " - " + saved.getPrincipalAmount(),
                "LOAN_DISBURSED");
        return LoanResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoan(Long loanId, Long userId) {
        Loan loan = findLoanForUser(loanId, userId);
        return LoanResponse.from(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponse> listMyLoans(Long userId, Pageable pageable) {
        return loanRepository.findByUserId(userId, pageable).map(LoanResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponse> listLoansByStatus(Loan.LoanStatus status, Pageable pageable) {
        return loanRepository.findByStatus(status, pageable).map(LoanResponse::from);
    }

    @Override
    @Transactional
    public LoanPaymentResponse makePayment(Long loanId, LoanPaymentRequest request, Long userId) {
        Loan loan = findLoanForUser(loanId, userId);
        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new BankingException("Loan is not active");
        }

        LoanPayment payment = loanPaymentRepository
                .findByLoanIdAndInstallmentNumber(loanId, request.getInstallmentNumber())
                .orElseThrow(() -> new BankingException("Installment not found"));

        if (payment.getStatus() == LoanPayment.PaymentStatus.PAID) {
            throw new BankingException("Installment already paid");
        }

        // Split payment between principal and interest proportionally
        BigDecimal totalDue = payment.getTotalDue();
        BigDecimal principalRatio = payment.getPrincipalDue()
                .divide(totalDue, 10, RoundingMode.HALF_UP);
        BigDecimal interestRatio = payment.getInterestDue()
                .divide(totalDue, 10, RoundingMode.HALF_UP);

        BigDecimal principalPaid = request.getPaymentAmount().multiply(principalRatio)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal interestPaid = request.getPaymentAmount().multiply(interestRatio)
                .setScale(2, RoundingMode.HALF_UP);

        payment.setPrincipalPaid(principalPaid);
        payment.setInterestPaid(interestPaid);
        payment.setTotalPaid(request.getPaymentAmount());
        payment.setPaidDate(LocalDate.now());
        payment.setStatus(LoanPayment.PaymentStatus.PAID);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionReference(request.getTransactionReference());

        // Update loan totals
        BigDecimal newOutstanding = loan.getOutstandingBalance().subtract(principalPaid);
        payment.setOutstandingAfter(newOutstanding);
        loan.setOutstandingBalance(newOutstanding);
        loan.setTotalPaid(loan.getTotalPaid().add(request.getPaymentAmount()));
        loan.setTotalInterest(loan.getTotalInterest().add(interestPaid));
        loan.setLastPaymentDate(LocalDate.now());
        loan.setRemainingPayments(loan.getRemainingPayments() - 1);

        // Find next scheduled payment
        loanPaymentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId).stream()
                .filter(p -> p.getStatus() == LoanPayment.PaymentStatus.SCHEDULED
                          || p.getStatus() == LoanPayment.PaymentStatus.DUE)
                .findFirst()
                .ifPresentOrElse(
                    p -> loan.setNextPaymentDate(p.getDueDate()),
                    () -> {
                        loan.setStatus(Loan.LoanStatus.CLOSED);
                        loan.setClosedDate(LocalDate.now());
                    }
                );

        loanRepository.save(loan);
        LoanPayment saved = loanPaymentRepository.save(payment);
        return LoanPaymentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanPaymentResponse> getSchedule(Long loanId, Long userId) {
        findLoanForUser(loanId, userId);
        return loanPaymentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId).stream()
                .map(LoanPaymentResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public int markOverduePayments() {
        List<LoanPayment> overdue = loanPaymentRepository.findOverdueCandidates(LocalDate.now());
        for (LoanPayment p : overdue) {
            p.setStatus(LoanPayment.PaymentStatus.OVERDUE);
            Loan loan = p.getLoan();
            loan.setMissedPayments(loan.getMissedPayments() + 1);
            loanRepository.save(loan);
        }
        loanPaymentRepository.saveAll(overdue);
        return overdue.size();
    }

    // ---------- helpers ----------

    private String generateLoanNumber() {
        long seq = loanRepository.maxId() + 1;
        return "LN" + String.format("%010d", seq);
    }

    private Loan findLoanForUser(Long loanId, Long userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BankingException("Loan not found"));
        if (loan.getUser() == null || !Objects.equals(loan.getUser().getId(), userId)) {
            throw new BankingException("Loan not found");
        }
        return loan;
    }

    private String gradeRisk(Integer creditScore) {
        if (creditScore == null) return "C";
        if (creditScore >= 800) return "A+";
        if (creditScore >= 740) return "A";
        if (creditScore >= 670) return "B";
        if (creditScore >= 580) return "C";
        if (creditScore >= 500) return "D";
        return "E";
    }

    private BigDecimal computeEmi(BigDecimal principal, BigDecimal monthlyRate, int months) {
        if (monthlyRate.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal pow = onePlusR.pow(months);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private void generateSchedule(Loan loan) {
        BigDecimal balance = loan.getPrincipalAmount();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal emi = loan.getMonthlyPayment();
        List<LoanPayment> schedule = new ArrayList<>();

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            BigDecimal interest = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = emi.subtract(interest).setScale(2, RoundingMode.HALF_UP);
            if (i == loan.getTermMonths()) {
                principalPart = balance;
                emi = principalPart.add(interest);
            }
            balance = balance.subtract(principalPart);

            LoanPayment p = new LoanPayment();
            p.setLoan(loan);
            p.setInstallmentNumber(i);
            p.setDueDate(loan.getFirstPaymentDate().plusMonths(i - 1));
            p.setPaymentAmount(emi);
            p.setPrincipalAmount(principalPart);
            p.setInterestAmount(interest);
            p.setPrincipalDue(principalPart);
            p.setInterestDue(interest);
            p.setTotalDue(emi);
            p.setOutstandingAfter(balance);
            p.setStatus(LoanPayment.PaymentStatus.SCHEDULED);
            p.setPaymentType(LoanPayment.PaymentType.EMI);
            schedule.add(p);
        }
        loanPaymentRepository.saveAll(schedule);
        loan.getPayments().addAll(schedule);
    }
}
