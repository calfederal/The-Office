package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.FixedDeposit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {

    Optional<FixedDeposit> findByFdNumber(String fdNumber);

    Page<FixedDeposit> findByUserId(Long userId, Pageable pageable);

    Page<FixedDeposit> findByStatus(FixedDeposit.FdStatus status, Pageable pageable);

    @Query("SELECT fd FROM FixedDeposit fd WHERE fd.status = 'ACTIVE' AND fd.maturityDate <= :today")
    List<FixedDeposit> findMaturedAsOf(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(MAX(fd.id), 0) FROM FixedDeposit fd")
    long maxId();
}
