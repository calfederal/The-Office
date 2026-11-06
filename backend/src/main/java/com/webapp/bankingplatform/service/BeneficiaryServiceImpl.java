package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.dto.BeneficiaryRequest;
import com.webapp.bankingplatform.entity.Beneficiary;
import com.webapp.bankingplatform.entity.User;
import com.webapp.bankingplatform.repository.BeneficiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    @Override
    @Transactional
    public Beneficiary create(User owner, BeneficiaryRequest req) {
        if (beneficiaryRepository.existsByOwnerAndNicknameIgnoreCase(owner, req.getNickname())) {
            throw new IllegalArgumentException("Beneficiary nickname already exists");
        }
        Beneficiary b = Beneficiary.builder()
                .owner(owner)
                .nickname(req.getNickname())
                .beneficiaryName(req.getBeneficiaryName())
                .bankName(req.getBankName())
                .routingNumber(req.getRoutingNumber())
                .accountNumber(req.getAccountNumber())
                .accountType(req.getAccountType())
                .transferType(req.getTransferType())
                .addressLine1(req.getAddressLine1())
                .city(req.getCity())
                .state(req.getState())
                .postalCode(req.getPostalCode())
                .countryCode(req.getCountryCode())
                .phoneNumber(req.getPhoneNumber())
                .email(req.getEmail())
                .active(true)
                .verified(false)
                .build();
        return beneficiaryRepository.save(b);
    }

    @Override
    @Transactional
    public Beneficiary update(User owner, Long id, BeneficiaryRequest req) {
        Beneficiary b = get(owner, id);
        b.setNickname(req.getNickname());
        b.setBeneficiaryName(req.getBeneficiaryName());
        b.setBankName(req.getBankName());
        b.setRoutingNumber(req.getRoutingNumber());
        b.setAccountNumber(req.getAccountNumber());
        b.setAccountType(req.getAccountType());
        b.setTransferType(req.getTransferType());
        b.setAddressLine1(req.getAddressLine1());
        b.setCity(req.getCity());
        b.setState(req.getState());
        b.setPostalCode(req.getPostalCode());
        b.setCountryCode(req.getCountryCode());
        b.setPhoneNumber(req.getPhoneNumber());
        b.setEmail(req.getEmail());
        return beneficiaryRepository.save(b);
    }

    @Override
    @Transactional
    public void delete(User owner, Long id) {
        Beneficiary b = get(owner, id);
        beneficiaryRepository.delete(b);
    }

    @Override
    @Transactional(readOnly = true)
    public Beneficiary get(User owner, Long id) {
        return beneficiaryRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Beneficiary> list(User owner) {
        return beneficiaryRepository.findByOwnerAndActiveTrueOrderByCreatedAtDesc(owner);
    }
}
