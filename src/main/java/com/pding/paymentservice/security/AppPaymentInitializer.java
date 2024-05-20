package com.pding.paymentservice.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.InAppProduct;
import com.google.api.services.androidpublisher.model.InappproductsListResponse;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Component
public class AppPaymentInitializer {

    @Autowired
    private SsmClient ssmClient;

    @Value("${aws.param.firebaseKey}")
    private String firebaseKey;


    @Value("${app.package.name}")
    private String appPackageName;

    AndroidPublisher androidPublisher;

    public String getFirebaseServiceAccount() {
        GetParameterResponse response = ssmClient.getParameter(
                GetParameterRequest.builder()
                        .name(firebaseKey)
                        .build()
        );

        return response.parameter().value();
    }


    @PostConstruct
    public void initialize() throws IOException {
        try {
            String key = getFirebaseServiceAccount();
            InputStream inputStream = new ByteArrayInputStream(key.getBytes());

            GoogleCredential credential = GoogleCredential.fromStream(inputStream)
                    .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

            androidPublisher = new AndroidPublisher.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("com.pding.android.dev")
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    public ProductPurchase getProductPurchase(String productId, String token) throws IOException {
        return androidPublisher.purchases().products().get(appPackageName, productId, token).execute();
    }

    public List<InAppProduct> listInAppProducts() throws IOException {
        AndroidPublisher.Inappproducts.List request = androidPublisher.inappproducts().list(appPackageName);
        InappproductsListResponse response = request.execute();
        return response.getInappproduct();
    }
}
