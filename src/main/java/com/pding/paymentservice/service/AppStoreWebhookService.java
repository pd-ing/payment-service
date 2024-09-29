package com.pding.paymentservice.service;

import com.google.gson.Gson;
import com.pding.paymentservice.BaseService;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.payload.response.ResponseBodyV2DecodedPayload;
import com.pding.paymentservice.paymentclients.ios.IOSPaymentInitializer;
import com.pding.paymentservice.paymentclients.ios.TransactionDetails;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static com.pding.paymentservice.paymentclients.ios.IOSPaymentInitializer.parseTransaction;

@Service
@Log4j2
@RequiredArgsConstructor
public class AppStoreWebhookService extends BaseService {

    private final PaymentService paymentService;

    private final OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    public void handle(ResponseBodyV2DecodedPayload decodedPayload) throws Exception {
        log.info("Start handling App Store Webhook");
        Gson gson = new Gson();

        String notificationType = decodedPayload.getNotificationType();
        String signedTransactionInfo = decodedPayload.getData().getSignedTransactionInfo();
        String decodedTransactionInfo = IOSPaymentInitializer.getTransactionBodyFromEncodedTransactionResponse(signedTransactionInfo);
        log.info("Decoded Transaction Info: {}", decodedTransactionInfo);
        JSONObject jsonObject = new JSONObject(decodedTransactionInfo);

        TransactionDetails txnDetails = parseTransaction(jsonObject);
        log.info("Transaction Details: " + gson.toJson(txnDetails));
        String txnId = txnDetails.getTransactionId();

        if ("ONE_TIME_CHARGE".equalsIgnoreCase(notificationType)) {
            String appAccountToken = txnDetails.getAppAccountToken();
            if (appAccountToken == null) {
                log.error("App Account Token is null for txnId: {}", txnId);
                return;
            }

            Optional<String> optionalUserId = otherServicesTablesNativeQueryRepository.getUserIdByUUID(appAccountToken);
            if (optionalUserId.isEmpty()) {
                log.error("User not found for appAccountToken: {}", appAccountToken);
                return;
            }
            String userId = optionalUserId.get();

            if (paymentService.checkIfTxnIdExists(txnDetails.getTransactionId())) {
                return;
            }

            BigDecimal purchaseLeaves = txnDetails.getLeafs();
            String paymentMethod = "IOS_Store";
            String currency = txnDetails.getCurrency();
            BigDecimal amountInCents = txnDetails.getPrice();

            log.info("notification type: ONE_TIME_CHARGE,  User completed payment in IOS Store. Adding {} leafs for user: {}", purchaseLeaves, userId);

            String message = paymentService.completePaymentToBuyLeafs(
                    userId,
                    new BigDecimal(0),
                    purchaseLeaves,
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(txnDetails.getOriginalPurchaseDate()), ZoneId.systemDefault()),
                    txnId,
                    TransactionType.PAYMENT_COMPLETED.getDisplayName(),
                    amountInCents,
                    paymentMethod,
                    currency,
                    "Added " + purchaseLeaves + " leafs successfully for user.",
                    null
            );
            return;
        }

        if ("REFUND".equalsIgnoreCase(notificationType)) {
            log.info("notification type: REFUND,  User request refund in IOS Store. Refunding {} leafs for user: {}", txnDetails.getLeafs(), txnDetails.getAppAccountToken());
            paymentService.completeRefundLeafs(txnId);
            return;
        }

        if("REFUND_DECLINED".equalsIgnoreCase(notificationType) || "REFUND_REVERSED".equalsIgnoreCase(notificationType)) {
            log.info("notification type: {},  IOS Store canncel the user's refund. cancel refund {} leafs for user: {}", notificationType, txnDetails.getLeafs(), txnDetails.getAppAccountToken());
            try {
                paymentService.cancelRefundLeafs(txnId);
            } catch (Exception e) {
                pdLogger.logException(e);
            }
            return;
        }

    }

}
