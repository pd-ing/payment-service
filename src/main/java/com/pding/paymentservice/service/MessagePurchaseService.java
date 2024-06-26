package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.MessagePurchase;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.enums.NotificaitonDataType;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.repository.MessagePurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
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

    @Transactional
    public String CreateMessageTransaction(String userId,
                                           String pdUserId,
                                           BigDecimal leafsTransacted,
                                           String messagedId,
                                           Boolean isGift,
                                           String giftId,
                                           Boolean notifyPd) {
        walletService.deductLeafsFromWallet(userId, leafsTransacted);

        MessagePurchase transaction = new MessagePurchase(userId, pdUserId, leafsTransacted, messagedId, isGift, giftId);
        MessagePurchase messagePurchase = messagePurchaseRepository.save(transaction);
        pdLogger.logInfo("MESSAGE_PURCHASE", "Message purchase record created with details UserId : " + userId + " ,messageId : " + messagePurchase + ", leafs : " + leafsTransacted + ", pdUserId : " + pdUserId);

        earningService.addLeafsToEarning(pdUserId, leafsTransacted);
        ledgerService.saveToLedger(messagePurchase.getMessageId(), new BigDecimal(0), leafsTransacted, TransactionType.TEXT_MESSAGE, userId);
        pdLogger.logInfo("MESSAGE_PURCHASE", "Message purchase details recorded in LEDGER MessageId : " + messagedId + ", leafs : " + leafsTransacted + ", TransactionType : " + TransactionType.TEXT_MESSAGE);

        if (notifyPd) {
            try {
                Map<String, String> data = new HashMap<>();
                data.put("NotificationType", NotificaitonDataType.GIFT_RECEIVE.getDisplayName());
                data.put("GiftId", giftId);
                data.put("UserId", pdUserId);
                data.put("leafsTransacted", leafsTransacted.toString());
                fcmService.sendNotification(pdUserId, data);
            } catch (Exception e) {
                pdLogger.logException(e);
            }
        }

        return "Leafs charge was successful";
    }
}
