package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findBySourceAccount_AccountNumberOrTargetAccount_AccountNumber(
            String sourceAccountNumber, String targetAccountNumber);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.sourceAccount.id = :accountId AND t.transactionDate >= :since")
    Double sumOutgoingSince(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);
}
