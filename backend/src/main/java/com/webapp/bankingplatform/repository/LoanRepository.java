package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    Page<Loan> findByUserId(Long userId, Pageable pageable);

    Page<Loan> findByStatus(Loan.LoanStatus status, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user.id = :userId AND l.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Loan.LoanStatus status);

    @Query("SELECT COALESCE(MAX(l.id), 0) FROM Loan l")
    long maxId();
}
