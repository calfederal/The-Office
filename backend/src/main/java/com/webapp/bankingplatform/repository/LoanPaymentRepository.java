package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    List<LoanPayment> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);

    Optional<LoanPayment> findByLoanIdAndInstallmentNumber(Long loanId, Integer installmentNumber);

    @Query("SELECT lp FROM LoanPayment lp WHERE lp.status IN ('SCHEDULED', 'DUE') AND lp.dueDate < :today")
    List<LoanPayment> findOverdueCandidates(@Param("today") LocalDate today);

    @Query("SELECT COUNT(lp) FROM LoanPayment lp WHERE lp.loan.id = :loanId AND lp.status = 'PAID'")
    long countPaidByLoanId(@Param("loanId") Long loanId);
}
