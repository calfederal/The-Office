package com.webapp.bankingplatform.service;

import com.webapp.bankingplatform.dto.FdApplicationRequest;
import com.webapp.bankingplatform.dto.FdResponse;
import com.webapp.bankingplatform.entity.FixedDeposit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FdService {

    FdResponse openFd(Long userId, FdApplicationRequest request);

    FdResponse getFd(Long fdId);

    Page<FdResponse> listMyFds(Long userId, Pageable pageable);

    Page<FdResponse> listFdsByStatus(FixedDeposit.FdStatus status, Pageable pageable);

    FdResponse closeFdPremature(Long fdId);

    FdResponse renewFd(Long fdId);

    int processMaturedFds();
}
