package com.pding.paymentservice.service;

import com.google.gson.Gson;
import com.pding.paymentservice.BaseService;
import com.pding.paymentservice.payload.response.ResponseBodyV2DecodedPayload;
import com.pding.paymentservice.paymentclients.ios.IOSPaymentInitializer;
import com.pding.paymentservice.paymentclients.ios.TransactionDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.stereotype.Service;


import static com.pding.paymentservice.paymentclients.ios.IOSPaymentInitializer.parseTransaction;

@Service
@Log4j2
@RequiredArgsConstructor
public class AppStoreWebhookService extends BaseService {

    private final PaymentService paymentService;

    private final IOSPaymentInitializer iosPaymentInitializer;

    public void handle(ResponseBodyV2DecodedPayload decodedPayload) throws Exception {

        Gson gson = new Gson();
        String notificationType = decodedPayload.getNotificationType();

        String signedTransactionInfo = decodedPayload.getData().getSignedTransactionInfo();
        String encodedTransactionInfo = IOSPaymentInitializer.getTransactionBodyFromEncodedTransactionResponse(signedTransactionInfo);

        JSONObject jsonObject = new JSONObject(encodedTransactionInfo);
        TransactionDetails txnDetails = parseTransaction(jsonObject);
        log.info("Transaction Details: " + gson.toJson(txnDetails));

        String txnId = txnDetails.getTransactionId();


        if ("ONE_TIME_CHARGE".equalsIgnoreCase(notificationType)) {
            // TODO: now this completed by /buyLeafsIOS endpoint
            return;
        }

        if ("REFUND".equalsIgnoreCase(notificationType)) {
            paymentService.completeRefundLeafs(txnId);
        }
    }

}
