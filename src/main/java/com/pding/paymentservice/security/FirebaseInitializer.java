package com.pding.paymentservice.security;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


@Component
public class FirebaseInitializer implements CommandLineRunner {

    @Value("${app.config.firebase-admin-keys}")
    private String firebaseCreds;

    @Value("${firebase.db.url}")
    private String dbUrl;

    AndroidPublisher androidPublisher;

    @Override
    public void run(String... args) throws Exception {

        try {
            String key = firebaseCreds;
            InputStream inputStream = new ByteArrayInputStream(key.getBytes());
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .setDatabaseUrl(dbUrl)
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
