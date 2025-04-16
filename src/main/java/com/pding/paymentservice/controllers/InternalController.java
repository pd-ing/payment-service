package com.pding.paymentservice.controllers;

import com.pding.paymentservice.service.VideoPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
public class InternalController {
    @Autowired
    VideoPurchaseService videoPurchaseService;

    @PostMapping("/checkIfVideoPurchasedExists")
    public ResponseEntity<?> checkIfVideoIsPurchased(@RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.isVideoPurchasedExists(videoId);
    }
}
