package com.pding.paymentservice.controllers;

import com.google.api.services.androidpublisher.model.InAppProduct;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.paymentclients.google.AppPaymentInitializer;
import com.pding.paymentservice.paymentclients.ios.IOSPaymentInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment/app")
public class AppPaymentController {

    @Autowired
    AppPaymentInitializer appPaymentInitializer;

    @Autowired
    IOSPaymentInitializer iosPaymentInitializer;


//    @GetMapping("/listProducts")
//    ResponseEntity<?> getProductList() {
//        try {
//            List<InAppProduct> inAppProducts = appPaymentInitializer.listInAppProducts().stream().filter(inAppProduct -> inAppProduct.getStatus().equals("active")).toList();
//            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, inAppProducts));
//        } catch (Exception e) {
//            return ResponseEntity.ok().body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
//        }
//    }

    @GetMapping("/listProducts")
    ResponseEntity<?> getProductListV2(@RequestHeader(value = "PDing-Platform", required = false) String platform) {
        try {
            if ("android".equalsIgnoreCase(platform)) {
                List<InAppProduct> inAppProducts = appPaymentInitializer.listInAppProductsPlayStore();
                return ResponseEntity.ok().body(new GenericListDataResponse<>(null, inAppProducts));
            } else {
                return ResponseEntity.ok().body(new GenericListDataResponse<>(null, iosPaymentInitializer.listProduct()));
            }
        } catch (Exception e) {
            return ResponseEntity.ok().body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

}
