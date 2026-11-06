package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.dto.BeneficiaryRequest;
import com.webapp.bankingplatform.entity.Beneficiary;
import com.webapp.bankingplatform.entity.User;

import java.util.List;

public interface BeneficiaryService {
    Beneficiary create(User owner, BeneficiaryRequest request);
    Beneficiary update(User owner, Long id, BeneficiaryRequest request);
    void delete(User owner, Long id);
    Beneficiary get(User owner, Long id);
    List<Beneficiary> list(User owner);
}
