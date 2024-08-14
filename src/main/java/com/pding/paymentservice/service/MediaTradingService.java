package com.pding.paymentservice.service;

import com.google.gson.Gson;
import com.pding.paymentservice.models.InChatMediaTrading;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.payload.request.AddMediaTrandingRequest;
import com.pding.paymentservice.repository.MediaTradingRepository;
import com.pding.paymentservice.security.AuthHelper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MediaTradingService {
    private final MediaTradingRepository mediaTradingRepository;
    private final WalletService walletService;
    private final EarningService earningService;
    private final LedgerService ledgerService;
    private final SqsTemplate sqsTemplate;
    private final AuthHelper authHelper;

    public InChatMediaTrading saveMediaTrading(AddMediaTrandingRequest addMediaTrandingRequest) {


        Optional<InChatMediaTrading> inChatMediaTradingOpt = mediaTradingRepository.findByMessageId(addMediaTrandingRequest.getMessageId());

        if (inChatMediaTradingOpt.isPresent()) {
            InChatMediaTrading inChatMediaTrading = inChatMediaTradingOpt.get();

            inChatMediaTrading.setIsCancel(addMediaTrandingRequest.getIsCancel());
            inChatMediaTrading.setTransactionStatus(addMediaTrandingRequest.getTransactionStatus());
            return mediaTradingRepository.save(inChatMediaTrading);
        }


        InChatMediaTrading inChatMediaTrading = InChatMediaTrading.builder()
                .userId(null) //no need
                .pdId(addMediaTrandingRequest.getPdId())
                .messageId(addMediaTrandingRequest.getMessageId())
                .leafsToCharge(addMediaTrandingRequest.getLeafsToCharge().toString())
                .attachments(addMediaTrandingRequest.getAttachments())
                .isCancel(addMediaTrandingRequest.getIsCancel())
                .transactionStatus(addMediaTrandingRequest.getTransactionStatus())
                .cid(addMediaTrandingRequest.getCid())
                .build();

        return mediaTradingRepository.save(inChatMediaTrading);

    }

    @Transactional
    public InChatMediaTrading buyMediaTrade(String messageId) {

        InChatMediaTrading inChatMediaTrading = mediaTradingRepository.findByMessageId(messageId).orElseThrow(
                () -> new RuntimeException("Media Trading not found")
        );

        if(inChatMediaTrading.getIsCancel()) {
            throw new RuntimeException("Media Trading is cancelled");
        }

        if ("PAID".equalsIgnoreCase(inChatMediaTrading.getTransactionStatus())) {
            throw new RuntimeException("Media already paid");
        }

        BigDecimal leafsToCharge = new BigDecimal(inChatMediaTrading.getLeafsToCharge());
        String pdUserId = inChatMediaTrading.getPdId();
        String userId = authHelper.getUserId();

        walletService.deductLeafsFromWallet(userId, leafsToCharge);
        earningService.addLeafsToEarning(pdUserId, leafsToCharge);
        ledgerService.saveToLedger(messageId, new BigDecimal(0), leafsToCharge, TransactionType.MEDIA_TRADING, userId);

        inChatMediaTrading.setTransactionStatus("PAID");
        inChatMediaTrading.setUserId(userId);
        mediaTradingRepository.save(inChatMediaTrading);

        //raise event to update message
        Gson gson = new Gson();
        sqsTemplate.send("InChatMediaTrading", gson.toJson(inChatMediaTrading));

        return inChatMediaTrading;
    }
}
