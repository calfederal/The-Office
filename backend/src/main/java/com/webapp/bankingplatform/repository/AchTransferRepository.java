package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.AchTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AchTransferRepository extends JpaRepository<AchTransfer, Long> {

    Optional<AchTransfer> findByTraceNumber(String traceNumber);

    List<AchTransfer> findByStatus(String status);

    List<AchTransfer> findByAccount_IdAndStatus(Long accountId, String status);

    @Query("SELECT t FROM AchTransfer t WHERE t.batch IS NULL AND t.status = :status ORDER BY t.effectiveDate, t.id")
    List<AchTransfer> findUnbatchedByStatus(@Param("status") String status);

    @Query("SELECT t FROM AchTransfer t WHERE t.account.id = :accountId ORDER BY t.effectiveDate DESC, t.id DESC")
    List<AchTransfer> findByAccountIdOrderByEffectiveDateDesc(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(t.traceNumber, 9, 7) AS long)), 0) FROM AchTransfer t WHERE SUBSTRING(t.traceNumber, 1, 8) = :odfiPrefix")
    long maxSequenceForOdfi(@Param("odfiPrefix") String odfiPrefix);

    long countByEffectiveDate(LocalDate effectiveDate);
}
