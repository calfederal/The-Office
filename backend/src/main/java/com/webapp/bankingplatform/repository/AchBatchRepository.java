package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.AchBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchBatchRepository extends JpaRepository<AchBatch, Long> {

    Optional<AchBatch> findByBatchNumber(String batchNumber);

    List<AchBatch> findByStatus(String status);

    List<AchBatch> findByFile_Id(Long fileId);
}
