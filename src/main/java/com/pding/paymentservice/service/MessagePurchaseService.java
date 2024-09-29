package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.MessagePurchase;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.enums.NotificaitonDataType;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.repository.CallPurchaseRepository;
import com.pding.paymentservice.repository.MessagePurchaseRepository;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.util.FirebaseRealtimeDbHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MessagePurchaseService {

    @Autowired
    WalletService walletService;

    @Autowired
    MessagePurchaseRepository messagePurchaseRepository;

    @Autowired
    EarningService earningService;

    @Autowired
    LedgerService ledgerService;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    FcmService fcmService;

    @Autowired
    CallPurchaseRepository callPurchaseRepository;

    @Autowired
    FirebaseRealtimeDbHelper firebaseRealtimeDbHelper;

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    @Transactional
    public String CreateMessageTransaction(String userId,
                                           String pdUserId,
                                           BigDecimal leafsTransacted,
                                           String messagedId,
                                           Boolean isGift,
                                           String giftId,
                                           Boolean notifyPd) {
        log.info("Creating message transaction for userId {}, pdUserId {}, leafsTransacted {}, messageId {}, isGift {}, giftId {}, notifyPd {}",
                userId, pdUserId, leafsTransacted, messagedId, isGift, giftId, notifyPd);
        walletService.deductLeafsFromWallet(userId, leafsTransacted);

        MessagePurchase transaction = new MessagePurchase(userId, pdUserId, leafsTransacted, messagedId, isGift, giftId, LocalDateTime.now());
        MessagePurchase messagePurchase = messagePurchaseRepository.save(transaction);
        log.info("Message purchase record created with details UserId : {}, messageId : {}, leafs : {}, pdUserId : {}",
                userId, messagePurchase, leafsTransacted, pdUserId);
//        pdLogger.logInfo("MESSAGE_PURCHASE", "Message purchase record created with details UserId : " + userId + " ,messageId : " + messagePurchase + ", leafs : " + leafsTransacted + ", pdUserId : " + pdUserId);

        earningService.addLeafsToEarning(pdUserId, leafsTransacted);
        ledgerService.saveToLedger(messagePurchase.getMessageId(), new BigDecimal(0), leafsTransacted, TransactionType.TEXT_MESSAGE, userId);

        if(notifyPd) {
            try {
                Map<String, String> data = new LinkedHashMap<>();
                data.put("NotificationType", NotificaitonDataType.GIFT_RECEIVE.getDisplayName());
                data.put("GiftId", giftId);
                data.put("userId", userId);
                data.put("leafsTransacted", leafsTransacted.toString());
                data.put("notifyPd", notifyPd.toString());
                data.put("nickname", otherServicesTablesNativeQueryRepository.getNicknameByUserId(userId).orElse("User"));
                fcmService.sendNotification(pdUserId, data);
            } catch (Exception e) {
                pdLogger.logException(e);
            }
        }
        log.info("Message transaction created for userId {}, pdUserId {}, leafsTransacted {}, messageId {}, isGift {}, giftId {}, notifyPd {}",
                userId, pdUserId, leafsTransacted, messagedId, isGift, giftId, notifyPd);

        return "Leafs charge was successful";
    }


    //When sending the gift during call it is considered as the earning made during call
//    public void addCallTransactionEntryToRealTimeDatabase(String callId, BigDecimal leafsSpendToSendGift) {
//        try {
//            List<Object[]> callPurchaseList = callPurchaseRepository.findUserIdPdUserIdAndSumLeafsTransactedByCallId(callId);
//            for (Object[] callPurchase : callPurchaseList) {
//                String userId = callPurchase[0].toString();
//                String pdUserID = callPurchase[1].toString();
//                String leafsTransacted = callPurchase[2].toString();
//
//                //Update the info of user and his leafDeduction
//                firebaseRealtimeDbHelper.updateCallChargesDetailsInFirebase(
//                        userId,
//                        callId,
//                        new BigDecimal(leafsTransacted).add(leafsSpendToSendGift),
//                        new BigDecimal(0));
//
//                //Update the info of PD and his leaf Earning
//                firebaseRealtimeDbHelper.updateCallChargesDetailsInFirebase(
//                        pdUserID,
//                        callId,
//                        new BigDecimal(0),
//                        new BigDecimal(leafsTransacted).add(leafsSpendToSendGift));
//            }
//        } catch (Exception e) {
//            pdLogger.logException(e);
//        }
//    }
}
