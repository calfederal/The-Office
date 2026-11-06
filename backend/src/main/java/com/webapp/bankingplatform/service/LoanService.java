package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.dto.LoanApplicationRequest;
import com.webapp.bankingplatform.dto.LoanPaymentRequest;
import com.webapp.bankingplatform.dto.LoanPaymentResponse;
import com.webapp.bankingplatform.dto.LoanResponse;
import com.webapp.bankingplatform.entity.Loan;
import com.webapp.bankingplatform.entity.LoanPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LoanService {

    LoanResponse applyForLoan(Long userId, LoanApplicationRequest request);

    LoanResponse approveLoan(Long loanId, String approvedBy);

    LoanResponse rejectLoan(Long loanId, String reason);

    LoanResponse disburseLoan(Long loanId);

    LoanResponse getLoan(Long loanId);

    Page<LoanResponse> listMyLoans(Long userId, Pageable pageable);

    Page<LoanResponse> listLoansByStatus(Loan.LoanStatus status, Pageable pageable);

    LoanPaymentResponse makePayment(Long loanId, LoanPaymentRequest request);

    List<LoanPaymentResponse> getSchedule(Long loanId);

    int markOverduePayments();
}
