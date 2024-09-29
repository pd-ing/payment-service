package com.pding.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.pding.paymentservice.models.InChatMediaTrading;
import com.pding.paymentservice.models.enums.NotificaitonDataType;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.payload.request.AddMediaTrandingRequest;
import com.pding.paymentservice.repository.MediaTradingRepository;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.security.AuthHelper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaTradingService {
    private final MediaTradingRepository mediaTradingRepository;
    private final WalletService walletService;
    private final EarningService earningService;
    private final LedgerService ledgerService;
    private final SqsTemplate sqsTemplate;
    private final AuthHelper authHelper;
    private final FcmService fcmService;
    private final OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;
    public InChatMediaTrading saveMediaTrading(AddMediaTrandingRequest addMediaTrandingRequest) {
        log.info("New in chat media trading, userId: {}, pdId: {}, messageId: {}, leafsToCharge: {}",
                addMediaTrandingRequest.getUserId(), addMediaTrandingRequest.getPdId(), addMediaTrandingRequest.getMessageId(),
                addMediaTrandingRequest.getLeafsToCharge());
        Optional<InChatMediaTrading> inChatMediaTradingOpt = mediaTradingRepository.findByMessageId(addMediaTrandingRequest.getMessageId());

        if (inChatMediaTradingOpt.isPresent()) {
            InChatMediaTrading inChatMediaTrading = inChatMediaTradingOpt.get();

            inChatMediaTrading.setIsCancel(addMediaTrandingRequest.getIsCancel());
            inChatMediaTrading.setTransactionStatus(addMediaTrandingRequest.getTransactionStatus());
            return mediaTradingRepository.save(inChatMediaTrading);
        }


        InChatMediaTrading inChatMediaTrading = InChatMediaTrading.builder()
                .userId(addMediaTrandingRequest.getUserId())
                .pdId(addMediaTrandingRequest.getPdId())
                .messageId(addMediaTrandingRequest.getMessageId())
                .leafsToCharge(addMediaTrandingRequest.getLeafsToCharge().toString())
                .attachments(addMediaTrandingRequest.getAttachments())
                .isCancel(addMediaTrandingRequest.getIsCancel())
                .transactionStatus(addMediaTrandingRequest.getTransactionStatus())
                .cid(addMediaTrandingRequest.getCid())
                .lastUpdateDate(LocalDateTime.now())
                .build();

        InChatMediaTrading savedMediaTrading = mediaTradingRepository.save(inChatMediaTrading);

        // push notification to user
        Map<String, String> data = new LinkedHashMap<>();
        data.put("NotificationType", NotificaitonDataType.MEDIA_TRANSACTION_REQUEST_CHAT_ROOM.getDisplayName());
        data.put("pdId", addMediaTrandingRequest.getPdId());
        data.put("userId", addMediaTrandingRequest.getUserId());
        data.put("PdNickname", otherServicesTablesNativeQueryRepository.getNicknameByUserId(addMediaTrandingRequest.getPdId()).orElse("Following PD"));
        fcmService.sendAsyncNotification(addMediaTrandingRequest.getUserId(), data);

        return savedMediaTrading;
    }

    @Transactional
    public InChatMediaTrading buyMediaTrade(String messageId) throws JsonProcessingException {

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

        log.info("Buying media trade, messageId: {}", messageId);

        walletService.deductLeafsFromWallet(userId, leafsToCharge);
        earningService.addLeafsToEarning(pdUserId, leafsToCharge);
        ledgerService.saveToLedger(messageId, new BigDecimal(0), leafsToCharge, TransactionType.MEDIA_TRADING, userId);

        inChatMediaTrading.setTransactionStatus("PAID");
        inChatMediaTrading.setUserId(userId);
        inChatMediaTrading.setLastUpdateDate(LocalDateTime.now());
        mediaTradingRepository.save(inChatMediaTrading);

        raiseEventToUpdateOrDelete(inChatMediaTrading);

        //push notification to PD
        Map<String, String> data = new LinkedHashMap<>();
        data.put("NotificationType", NotificaitonDataType.MEDIA_PURCHASED.getDisplayName());
        data.put("userId", inChatMediaTrading.getUserId());
        data.put("nickname", otherServicesTablesNativeQueryRepository.getNicknameByUserId(userId).orElse("User"));
        fcmService.sendAsyncNotification(pdUserId, data);

        log.info("Media trade bought successfully, messageId: {}, userId: {}, pdId: {}", messageId, userId, pdUserId);
        return inChatMediaTrading;
    }

    private void raiseEventToUpdateOrDelete(InChatMediaTrading inChatMediaTrading) throws JsonProcessingException {
        //raise event to update message
        final ObjectMapper mapper = new ObjectMapper();
        ArrayNode attachments = (ArrayNode) mapper.readTree(inChatMediaTrading.getAttachments());
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("messageId", inChatMediaTrading.getMessageId());
        jsonNode.put("pdId", inChatMediaTrading.getPdId());
        jsonNode.put("attachments", attachments);
        jsonNode.put("isCancel", inChatMediaTrading.getIsCancel());
        jsonNode.put("transactionStatus", inChatMediaTrading.getTransactionStatus());
        jsonNode.put("cid", inChatMediaTrading.getCid());

        sqsTemplate.send("InChatMediaTrading", jsonNode.toString());
    }

    public void cancelMediaTrade(String messageId) throws JsonProcessingException {
        log.info("Cancelling media trade, messageId: {}", messageId);
        InChatMediaTrading inChatMediaTrading = mediaTradingRepository.findByMessageId(messageId).orElseThrow(
                () -> new RuntimeException("Media Trading not found")
        );

        if ("PAID".equalsIgnoreCase(inChatMediaTrading.getTransactionStatus())) {
            throw new RuntimeException("Media already paid");
        }

        inChatMediaTrading.setIsCancel(true);
        inChatMediaTrading.setTransactionStatus("CANCELLED");
        inChatMediaTrading.setLastUpdateDate(LocalDateTime.now());
        mediaTradingRepository.save(inChatMediaTrading);
        raiseEventToUpdateOrDelete(inChatMediaTrading);
        log.info("Media trade cancelled successfully by pdId {}, messageId: {}", inChatMediaTrading.getPdId(), messageId);
    }
}
