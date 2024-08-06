package com.pding.paymentservice.service;

import com.google.gson.Gson;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.notification.OneTimeProductNotification;
import com.pding.paymentservice.payload.notification.VoidedPurchaseNotification;
import com.pding.paymentservice.paymentclients.google.AppPaymentInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AppPurchaseWebhook {

    private static final Logger LOGGER = Logger.getLogger(AppPurchaseWebhook.class.getName());

    private final PdLogger pdLogger;

    private final PaymentService paymentService;

    @ServiceActivator(inputChannel = "pubsubInputChannel")
    public void messageReceiver(Message<String> message) throws Exception {
        String payload = message.getPayload();
        Gson gson = new Gson();
        if (payload.contains("oneTimeProductNotification")) {
            LOGGER.info("OneTimeProductNotification received! Payload: " + payload);
//            pdLogger.logInfo("BUY_LEAFS", "OneTimeProductNotification received! Payload: " + payload);

            Map<String, Object> payloadMap = gson.fromJson(payload, Map.class);
            Map<String, String> oneTimeProductNotificationMap = (Map<String, String>) payloadMap.get("oneTimeProductNotification");
            OneTimeProductNotification oneTimeProductNotification = gson.fromJson(gson.toJson(oneTimeProductNotificationMap), OneTimeProductNotification.class);

            String purchaseToken = oneTimeProductNotification.getPurchaseToken();

            switch (oneTimeProductNotification.getNotificationType()) {
                case 1:
                    LOGGER.info("do not handle this case");
                    // one-time product was successfully purchased by a user.
                    // This is completed by /buyLeafs API
                    break;
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
