package com.pding.paymentservice.service;

import com.pding.paymentservice.models.MissionExecution;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.payload.request.BuyMissionRequest;
import com.pding.paymentservice.repository.MissionRepository;
import com.pding.paymentservice.security.AuthHelper;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MissionTradingService {

    private final MissionRepository missionRepository;
    private final EarningService earningService;
    private final LedgerService ledgerService;
    private final WalletService walletService;
    private final AuthHelper authHelper;

    @Transactional
    public MissionExecution executeMission(BuyMissionRequest request) {
        String buyer = authHelper.getUserId();
        walletService.deductTreesFromWallet(buyer, request.getTreesOffered());
        earningService.addTreesToEarning(request.getPdUserId(), request.getTreesOffered());

        MissionExecution execution = MissionExecution.builder()
                .buyerUserId(buyer)
                .pdUserId(request.getPdUserId())
                .missionId(request.getMissionId())
                .streamId(request.getStreamId())
                .treesOffered(request.getTreesOffered())
                .executionDate(LocalDateTime.now())
                .build();

        MissionExecution savedExecution = missionRepository.save(execution);

        ledgerService.saveToLedger(
            savedExecution.getId(),
            request.getTreesOffered(),
            BigDecimal.ZERO,
            TransactionType.LS_MISSION,
            buyer
        );

        return savedExecution;
    }
}