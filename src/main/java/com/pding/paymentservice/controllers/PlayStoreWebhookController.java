package com.pding.paymentservice.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.request.PlayStoreWebhookPayload;
import com.pding.paymentservice.service.PlayStoreWebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")

public class PlayStoreWebhookController {

    @Autowired
    PlayStoreWebhookService playStoreWebhookService;

    @Autowired
    PdLogger pdLogger;
    private final GoogleIdTokenVerifier verifier =
            new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
//                    .setAudience(Collections.singletonList("example.com"))
                    .build();


    @PostMapping("/ggPlayStoreWebhook")
    public ResponseEntity<String> handleGGPlayStoreWebhook(@RequestBody PlayStoreWebhookPayload body, @RequestHeader Map<String, String> headers ) throws IOException {
        Gson gson = new Gson();
//        pdLogger.logInfo("Google Play Store Webhook", "Callback Successfull for  " + gson.toJson(body) + " with headers: " + gson.toJson(headers));
        String payload = new String(Base64.getDecoder().decode(body.getMessage().get("data")));
        playStoreWebhookService.handle(payload);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
