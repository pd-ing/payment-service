package com.pding.paymentservice.controllers;

import com.google.gson.Gson;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.request.AppStoreWebhookPayload;
import com.pding.paymentservice.payload.response.ResponseBodyV2DecodedPayload;
import com.pding.paymentservice.service.AppStoreWebhookService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class AppStoreWebhookController {

    private final PdLogger pdLogger;
    private final AppStoreWebhookService appStoreWebhookService;

    @PostMapping("/appStoreWebhook")
    public ResponseEntity<String> handleAppStoreWebhook(@RequestBody AppStoreWebhookPayload body) throws Exception {
//    public ResponseEntity<String> handleAppStoreWebhook(@RequestBody String payload) throws Exception {
        Gson gson = new Gson();
        String signedPayload = body.getSignedPayload();
        String[] split_string = signedPayload.split("\\.");
        String base64EncodedHeader = split_string[0];
        String base64EncodedPayload = split_string[1];
        String base64EncodedSignature = split_string[2];

        Base64 base64Url = new Base64(true);
        String header = new String(base64Url.decode(base64EncodedHeader));
        String payload = new String(base64Url.decode(base64EncodedPayload));
        String signature = new String(base64Url.decode(base64EncodedSignature));

        //TODO: verify signature

        ResponseBodyV2DecodedPayload decodedPayload = gson.fromJson(payload, ResponseBodyV2DecodedPayload.class);
        pdLogger.logInfo("App Store Webhook", "App Store Callback Successfull for  " + gson.toJson(decodedPayload));

        appStoreWebhookService.handle(decodedPayload);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
