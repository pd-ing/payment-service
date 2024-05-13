package com.pding.paymentservice.controllers;

import com.pding.paymentservice.app.config.LeafDonationConfig;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment/appConfig")
public class AppConfigController {
    @GetMapping("/leafDonationConfig")
    ResponseEntity<?> getLeafDonationConfig() {
        return ResponseEntity.ok().body(new GenericClassResponse<>(null, LeafDonationConfig.createConfig()));
    }
}
