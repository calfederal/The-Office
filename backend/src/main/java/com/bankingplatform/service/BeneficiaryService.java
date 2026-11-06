package com.bankingplatform.service;

import com.bankingplatform.dto.BeneficiaryRequest;
import com.bankingplatform.entity.Beneficiary;
import com.bankingplatform.entity.User;

import java.util.List;

public interface BeneficiaryService {
    Beneficiary create(User owner, BeneficiaryRequest request);
    Beneficiary update(User owner, Long id, BeneficiaryRequest request);
    void delete(User owner, Long id);
    Beneficiary get(User owner, Long id);
    List<Beneficiary> list(User owner);
}
