package com.pding.paymentservice.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.pding.paymentservice.PdLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class FirebaseInitializer implements CommandLineRunner {

    @Autowired
    private SsmClient ssmClient;

    @Value("${aws.param.firebaseKey}")
    private String firebaseKey;

    public String getFirebaseServiceAccount() {
        GetParameterResponse response = ssmClient.getParameter(
                GetParameterRequest.builder()
                        .name(firebaseKey)
                        .build()
        );

        return response.parameter().value();
    }

    @Override
    public void run(String... args) throws Exception {

        try {
            String key = getFirebaseServiceAccount();
            InputStream inputStream = new ByteArrayInputStream(key.getBytes());
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (FileNotFoundException e) {
            // TODO logs - service_account file not found
            throw new RuntimeException(e);
        } catch (IOException e) {
            //TODO logs
            throw new RuntimeException(e);
        }
    }

}
