package com.pding.paymentservice.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.pding.paymentservice.PdLogger;
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

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")

public class AppStoreWebhookController {

    @Autowired
    PdLogger pdLogger;


    @PostMapping("/appStoreWebhook")
    public ResponseEntity<String> handleAppStoreWebhook(@RequestBody Map<String, Object> body, @RequestHeader Map<String, String> headers ) {
        Gson gson = new Gson();
        pdLogger.logInfo("App Store Webhook", "App Store Callback Successfull for  " + gson.toJson(body) + " with headers: " + gson.toJson(headers));

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
