
package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.request.CheckImagePurchasedRequest;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.ImagePostPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class ImagePostPurchaseServiceController {

    @Autowired
    AuthHelper authHelper;
    @Autowired
    ImagePostPurchaseService imagePostPurchaseService;

    @PostMapping(value = "/buyImagePost")
    public ResponseEntity<?> buyImagePost(
            @RequestParam(value = "postId") String postId,
            @RequestParam(value = "leafAmount") BigDecimal leafAmount,
            @RequestParam(value = "postOwnerUserId") String postOwnerUserId) {
        String userId = authHelper.getUserId();
        return ResponseEntity.ok( new GenericClassResponse<>(null, imagePostPurchaseService.buyImagePost(userId, postId, leafAmount, postOwnerUserId)));
    }

    @PostMapping(value = "/isImagePostPurchased")
    public ResponseEntity<?> isImagePostPurchased(
            @RequestBody CheckImagePurchasedRequest request) {
        String userId = authHelper.getUserId();
        return ResponseEntity.ok( new GenericClassResponse<>(null, imagePostPurchaseService.isImagePostPurchased(userId, request.getPostIds())));
    }
}
