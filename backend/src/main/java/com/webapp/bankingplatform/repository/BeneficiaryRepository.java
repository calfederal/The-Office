package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.Beneficiary;
import com.webapp.bankingplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByOwnerOrderByCreatedAtDesc(User owner);

    List<Beneficiary> findByOwnerAndActiveTrueOrderByCreatedAtDesc(User owner);

    Optional<Beneficiary> findByIdAndOwner(Long id, User owner);

    boolean existsByOwnerAndNicknameIgnoreCase(User owner, String nickname);
}
