package com.pding.paymentservice.service;

import com.pding.paymentservice.models.LiveStreamPurchase;
import com.pding.paymentservice.payload.request.BuyLiveStreamRequest;
import com.pding.paymentservice.payload.response.BuyLiveStreamResponse;
import com.pding.paymentservice.repository.LiveStreamPurchaseRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.models.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class LiveStreamTradingService {

    private final LiveStreamPurchaseRepository livestreamPurchaseRepository;
    private final EarningService earningService;
    private final LedgerService ledgerService;
    private final WalletService walletService;
    private final AuthHelper authHelper;

    @Transactional
    public LiveStreamPurchase processLivestreamPurchase(BuyLiveStreamRequest request) {
        String buyer = authHelper.getUserId();
        walletService.deductTreesFromWallet(buyer, request.getTreesOffered());
        earningService.addTreesToEarning(request.getPdUserId(), request.getTreesOffered());
        LiveStreamPurchase purchase = LiveStreamPurchase.builder()
                .buyerUserId(buyer)
                .pdUserId(request.getPdUserId())
                .treesOffered(request.getTreesOffered())
                .livestreamId(request.getLivestreamId())
                .purchaseDate(LocalDateTime.now())
                .build();

        LiveStreamPurchase savedPurchase = livestreamPurchaseRepository.save(purchase);

        ledgerService.saveToLedger(request.getLivestreamId(), request.getTreesOffered(), BigDecimal.ZERO, TransactionType.LIVE_STREAM, buyer);
        return savedPurchase;
    }
}
