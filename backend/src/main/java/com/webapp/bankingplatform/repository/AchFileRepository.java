package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.AchFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchFileRepository extends JpaRepository<AchFile, Long> {

    Optional<AchFile> findByFileId(String fileId);

    List<AchFile> findByStatus(String status);
}
