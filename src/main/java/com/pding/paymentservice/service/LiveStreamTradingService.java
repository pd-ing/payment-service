package com.pding.paymentservice.service;

import com.pding.paymentservice.models.LiveStreamPurchase;
import com.pding.paymentservice.payload.request.BuyLivestreamRequest;
import com.pding.paymentservice.payload.response.BuyLivestreamResponse;
import com.pding.paymentservice.repository.LivestreamPurchaseRepository;
import com.pding.paymentservice.security.AuthHelper;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class LiveStreamTradingService {

    private final LivestreamPurchaseRepository livestreamPurchaseRepository;
    private final EarningService earningService;
    private final LedgerService ledgerService;
    private final WalletService walletService;
    private final AuthHelper authHelper;

    @Transactional
    public LiveStreamPurchase processLivestreamPurchase(BuyLivestreamRequest request) {
        String buyer = authHelper.getUserId();
        walletService.deductTreesFromWallet(buyer, request.getTreesOffered());
        earningService.addTreesToEarning(request.getPdUserId(), request.getTreesOffered());
        LiveStreamPurchase purchase = LiveStreamPurchase.builder()
                .buyer(buyer)
                .pdUserId(request.getPdUserId())
                .treesOffered(request.getTreesOffered())
                .timestamp(LocalDateTime.now())
                .build();

        LiveStreamPurchase savedPurchase = livestreamPurchaseRepository.save(purchase);

        ledgerService.saveToLedger(savedPurchase.getId(), request.getTreesOffered(), BigDecimal.ZERO, TransactionType.LIVESTREAM_PURCHASE, buyer);
        return savedPurchase;
    }
}
