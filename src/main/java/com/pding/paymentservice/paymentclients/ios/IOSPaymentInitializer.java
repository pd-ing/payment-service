package com.pding.paymentservice.paymentclients.ios;

import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.client.BearerTokenAuthenticator;
import com.apple.itunes.storekit.migration.ReceiptUtility;
import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.model.TransactionInfoResponse;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;


@Component
public class IOSPaymentInitializer {

    @Value("${app.ios.payment.issuerId}")
    String issuerId;

    @Value("${app.ios.payment.bundleId}")
    String bundleId;

    @Value("${app.ios.payment.inapp.purchase.keyId}")
    String inAppPurchaseKeyId;

    @Value("${app.ios.payment.inapp.purchase.private.key}")
    String inAppPurchasePrivateKey;


    @Value("${app.ios.payment.appstore.connect.keyId}")
    String appStoreConnectKeyId;

    @Value("${app.ios.payment.appstore.connect.private.key}")
    String appStoreConnectPrivateKey;


    @Value("${app.ios.payment.environment}")
    String environmentType;


    @Value("${app.ios.payment.appId}")
    String appId;

    public String getTransactionDetails(String transactionId) throws Exception {
        Environment environmentIOS;
        if (environmentType.equalsIgnoreCase("production")) {
            environmentIOS = Environment.PRODUCTION;
        } else {
            environmentIOS = Environment.SANDBOX;
        }

        AppStoreServerAPIClient client = new AppStoreServerAPIClient(inAppPurchasePrivateKey, inAppPurchaseKeyId, issuerId, bundleId, environmentIOS);

        TransactionInfoResponse transactionInfoResponse = client.getTransactionInfo(transactionId);

        return getTransactionBodyFromEncodedTransactionResponse(transactionInfoResponse.getSignedTransactionInfo());
    }

    public String getProductDetails(String productId) throws Exception {
        String token = generateTokenForAppStoreConnect();
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // Call This API , And pass the above token as header
        String getProductsUrl = "https://api.appstoreconnect.apple.com/v1/apps/" + appId + "/inAppPurchasesV2";

        // Build the request
        Request request = new Request.Builder()
                .url(getProductsUrl)
                .method("GET", null) // No need for a request body in a GET request
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Execute the request
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray dataArray = jsonObject.getJSONArray("data");

        // Parse the response to find the matching productId and return the name
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            JSONObject attributes = dataObject.getJSONObject("attributes");

            String currentProductId = attributes.getString("productId");
            if (currentProductId.equals(productId)) {
                return attributes.getString("name");
            }
        }

        // If no matching productId is found
        return null;
    }

    public TransactionDetails getLeafsToAdd(String transactionId, String productId) throws Exception {
        TransactionDetails txnDetails;
        String transaction = getTransactionDetails(transactionId);
        JSONObject jsonObject = new JSONObject(transaction);
        txnDetails = parseTransaction(jsonObject);

        // This is extra validation which we have added for security purpose, We take productId from App and validate it with the productId,
        // which we got in the transactionDetails
        if (!productId.equalsIgnoreCase(txnDetails.getProductId()))
            throw new RuntimeException("ProductId Provided in the payload dosent match with the productId found in Transaction Details");

        txnDetails.setLeafs(new BigDecimal(getProductDetails(txnDetails.getProductId())));
        return txnDetails;
    }

    public static TransactionDetails parseTransaction(JSONObject jsonObject) {
        TransactionDetails transaction = new TransactionDetails();

        transaction.setTransactionId(jsonObject.getString("transactionId"));
        transaction.setOriginalTransactionId(jsonObject.getString("originalTransactionId"));
        transaction.setBundleId(jsonObject.getString("bundleId"));
        transaction.setProductId(jsonObject.getString("productId"));
        transaction.setPurchaseDate(jsonObject.getLong("purchaseDate"));
        transaction.setOriginalPurchaseDate(jsonObject.getLong("originalPurchaseDate"));
        transaction.setQuantity(jsonObject.getInt("quantity"));
        transaction.setType(jsonObject.getString("type"));
        transaction.setInAppOwnershipType(jsonObject.getString("inAppOwnershipType"));
        transaction.setSignedDate(jsonObject.getLong("signedDate"));
        transaction.setEnvironment(jsonObject.getString("environment"));
        transaction.setTransactionReason(jsonObject.getString("transactionReason"));
        transaction.setStorefront(jsonObject.getString("storefront"));
        transaction.setStorefrontId(jsonObject.getString("storefrontId"));
        transaction.setPrice(new BigDecimal(jsonObject.getLong("price")));
        transaction.setCurrency(jsonObject.getString("currency"));

        return transaction;
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

    public String generateTokenForAppStoreConnect() throws Exception {
        ECPrivateKey privateKey = convertPemToPrivateKey(appStoreConnectPrivateKey);

        Instant now = Instant.now();
        String jwtToken = Jwts.builder()
                .setHeaderParam("alg", "ES256")
                .setHeaderParam("kid", appStoreConnectKeyId)
                .setHeaderParam("typ", "JWT")
                .setIssuer(issuerId)
                .setAudience("appstoreconnect-v1")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(900))) // 15 minutes
                // .addClaims(claims)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();

        return jwtToken;
    }

    public ECPrivateKey convertPemToPrivateKey(String pemKey) throws Exception {
        String cleanedPem = pemKey.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = java.util.Base64.getDecoder().decode(cleanedPem);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
