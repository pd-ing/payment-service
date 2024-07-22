package com.pding.paymentservice.paymentclients.ios;

import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.model.TransactionInfoResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.binary.Base64;


@Component
public class IOSPaymentInitializer {

    @Value("${app.ios.payment.issuerId}")
    String issuerId;

    @Value("${app.ios.payment.keyId}")
    String keyId;

    @Value("${app.ios.payment.bundleId}")
    String bundleId;

    @Value("${app.ios.payment.private.key}")
    String encodedKey;

    @Value("${app.ios.payment.environment}")
    String environmentType;

    public String getTransactionDetails(String transactionId) throws Exception {
//        String issuerId = "b4268b91-6668-437c-b8f6-2b50ddcb1274";
//        String keyId = "DQZM7PSLJX";
//        String bundleId = "com.pding.ping-mobile.dev";
//        Path filePath = Path.of("/Users/darshanmestry/Documents/Work/PDing/InAppPurchaseKeyIos.p8");
//        String encodedKey = Files.readString(filePath);
//        Environment environment = Environment.SANDBOX;

        Environment environmentIOS;
        if (environmentType.equalsIgnoreCase("production")) {
            environmentIOS = Environment.PRODUCTION;
        } else {
            environmentIOS = Environment.SANDBOX;
        }

        AppStoreServerAPIClient client = new AppStoreServerAPIClient(encodedKey, keyId, issuerId, bundleId, environmentIOS);

        TransactionInfoResponse transactionInfoResponse = client.getTransactionInfo(transactionId);

        return getTransactionBodyFromEncodedTransactionResponse(transactionInfoResponse.getSignedTransactionInfo());
    }


    private String getTransactionBodyFromEncodedTransactionResponse(String jwtToken) throws Exception {
        System.out.println("------------ Decode JWT ------------");
        String[] split_string = jwtToken.split("\\.");
        String base64EncodedHeader = split_string[0];
        String base64EncodedBody = split_string[1];
        String base64EncodedSignature = split_string[2];

        System.out.println("~~~~~~~~~ JWT Header ~~~~~~~");
        Base64 base64Url = new Base64(true);
        String header = new String(base64Url.decode(base64EncodedHeader));
        System.out.println("JWT Header : " + header);


        System.out.println("~~~~~~~~~ JWT Body ~~~~~~~");
        String body = new String(base64Url.decode(base64EncodedBody));
        System.out.println("JWT Body : " + body);
        return body;
    }
}
