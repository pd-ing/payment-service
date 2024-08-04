package com.pding.paymentservice.service;

import com.google.api.services.androidpublisher.model.InAppProduct;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.google.gson.Gson;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.payload.notification.OneTimeProductNotification;
import com.pding.paymentservice.payload.notification.VoidedPurchaseNotification;
import com.pding.paymentservice.paymentclients.google.AppPaymentInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AppPurchaseWebhook {

    private static final Logger LOGGER = Logger.getLogger(AppPurchaseWebhook.class.getName());

    private final AppPaymentInitializer appPaymentInitializer;
    private final PdLogger pdLogger;

    private final PaymentService paymentService;

    @ServiceActivator(inputChannel = "pubsubInputChannel")
    public void messageReceiver(Message<String> message) throws Exception {
        String payload = message.getPayload();
        Gson gson = new Gson();
        if (payload.contains("oneTimeProductNotification")) {
            LOGGER.info("OneTimeProductNotification received! Payload: " + payload);
            pdLogger.logInfo("BUY_LEAFS", "OneTimeProductNotification received! Payload: " + payload);

            Map<String, Object> payloadMap = gson.fromJson(payload, Map.class);
            Map<String, String> oneTimeProductNotificationMap = (Map<String, String>) payloadMap.get("oneTimeProductNotification");
            OneTimeProductNotification oneTimeProductNotification = gson.fromJson(gson.toJson(oneTimeProductNotificationMap), OneTimeProductNotification.class);

            String purchaseToken = oneTimeProductNotification.getPurchaseToken();
            String productId = oneTimeProductNotification.getSku();
            InAppProduct inAppProduct = appPaymentInitializer.getInAppProduct(productId);
            ProductPurchase productPurchase = appPaymentInitializer.getProductPurchase(productId, purchaseToken);

            String userId = productPurchase.getObfuscatedExternalAccountId();
            if (userId == null) return;

            if (paymentService.checkIfTxnIdExists(purchaseToken)) {
                LOGGER.info("TransactionId already exists in DB, transactionId: " + purchaseToken);
                pdLogger.logInfo("BUY_LEAFS", "TransactionId already exists in DB, transactionId: " + purchaseToken);
                return;
            }

            switch (oneTimeProductNotification.getNotificationType()) {
                case 1:
                    // one-time product was successfully purchased by a user.
                    try {
                        int purchaseLeaves = Integer.parseInt(productId.substring(productId.indexOf("_") + 1));
                        String txnId = purchaseToken;
                        String paymentMethod = "Google_Play_Store";
                        String currency = inAppProduct.getDefaultPrice().get("currency").toString();
                        String amountInCents = inAppProduct.getDefaultPrice().get("priceMicros").toString();
                        BigDecimal amount = new BigDecimal(amountInCents).divide(new BigDecimal(1000000)).setScale(2);

                        paymentService.completePaymentToBuyLeafs(
                                userId,
                                new BigDecimal(0),
                                new BigDecimal(purchaseLeaves),
                                LocalDateTime.ofInstant(Instant.ofEpochMilli(productPurchase.getPurchaseTimeMillis()), ZoneId.systemDefault()),
                                txnId,
                                TransactionType.PAYMENT_COMPLETED.getDisplayName(),
                                amount,
                                paymentMethod,
                                currency,
                                "Added " + purchaseLeaves + " leafs successfully for user.",
                                null
                        );
                        LOGGER.info("Successfully purchased by a user, transactionId: " + purchaseToken);
                    } catch (Exception e) {
                        pdLogger.logException(e);
                    } finally {
                        break;
                    }
                case 2:
                    // this event received when a user requests a refund for a one-time product.
                    try {
                        paymentService.completeRefundLeafs(purchaseToken);
                    } catch (Exception e) {
                        pdLogger.logException(e);
                    } finally {
                        break;
                    }
            }
            return;
        }

        // Check if is voided purchase notification
        if (payload.contains("voidedPurchaseNotification")) {
            pdLogger.logInfo("BUY_LEAFS", "VoidedPurchaseNotification received! Payload: " + payload);

            Map<String, Object> payloadMap = gson.fromJson(payload, Map.class);
            Map<String, String> voidedPurchaseNotificationMap = (Map<String, String>) payloadMap.get("voidedPurchaseNotification");
            VoidedPurchaseNotification voidedPurchaseNotification = gson.fromJson(gson.toJson(voidedPurchaseNotificationMap), VoidedPurchaseNotification.class);
            String purchaseToken = voidedPurchaseNotification.getPurchaseToken();

            if (voidedPurchaseNotification.getProductType() == 1) {
                // ignore because we don't have subscription in our app, just have one-time product
                return;
            }

            switch (voidedPurchaseNotification.getRefundType()) {
                case 1:
                    // this event received when Google completely refunds a user's purchase.
                    try {
                        paymentService.completeRefundLeafs(purchaseToken);
                    } catch (Exception e) {
                        pdLogger.logException(e);
                    } finally {
                        break;
                    }
                case 2:
                    // REFUND_TYPE_QUANTITY_BASED_PARTIAL_REFUND
                    // ignore because it is applicable only to multi-quantity purchases
                    break;
            }
        }

        // Check if is test notification
        if (payload.contains("testNotification")) {
            LOGGER.info("TestNotification received! Payload: " + payload);
        }
    }
}
