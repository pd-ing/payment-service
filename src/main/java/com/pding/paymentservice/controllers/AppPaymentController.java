package com.pding.paymentservice.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.androidpublisher.model.InAppProduct;
import com.pding.paymentservice.payload.response.DonationResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.security.AppPaymentInitializer;
import com.pding.paymentservice.security.FirebaseInitializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment/app")
public class AppPaymentController {

    @Autowired
    AppPaymentInitializer appPaymentInitializer;


    @GetMapping("/listProducts")
    ResponseEntity<?> getProductList() {
        try {
            List<InAppProduct> inAppProducts = appPaymentInitializer.listInAppProducts();
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, inAppProducts));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }
    
}
