package com.webapp.bankingplatform.repository;

import com.webapp.bankingplatform.entity.OtpInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpInfoRepository extends JpaRepository<OtpInfo, Long> {
    Optional<OtpInfo> findByPhoneNumberAndPurpose(String phoneNumber, String purpose);
    Optional<OtpInfo> findByPhoneNumberAndExpiryDateAfter(String phoneNumber, java.time.LocalDateTime expiryDate);
}
