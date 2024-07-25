package com.pding.paymentservice.controllers;

import com.apple.itunes.storekit.client.BearerTokenAuthenticator;
import com.google.api.services.androidpublisher.model.InAppProduct;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.payload.request.BuyLeafsRequest;
import com.pding.paymentservice.payload.request.BuyLeafsiOSRequest;
import com.pding.paymentservice.payload.request.PaymentDetailsRequest;
import com.pding.paymentservice.payload.request.PaymentInitFromBackendRequest;
import com.pding.paymentservice.payload.response.ClearPendingAndStalePaymentsResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.payload.response.MessageResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.paymentclients.google.AppPaymentInitializer;
import com.pding.paymentservice.paymentclients.ios.IOSPaymentInitializer;
import com.pding.paymentservice.paymentclients.ios.TransactionDetails;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.PaymentService;
import com.pding.paymentservice.paymentclients.stripe.StripeClient;
import com.pding.paymentservice.paymentclients.stripe.StripeClientResponse;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class PaymentServiceController {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    StripeClient stripeClient;

    @Autowired
    AppPaymentInitializer appPaymentInitializer;

    @Autowired
    IOSPaymentInitializer iosPaymentInitializer;

    @Autowired
    AuthHelper authHelper;


    @GetMapping(value = "/test")
    public ResponseEntity<?> sampleGet() {
        try {
            return ResponseEntity.ok()
                    .body(new MessageResponse("This is test API, With JWT Validation"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @PostMapping("/charge")
    public ResponseEntity<?> chargeCard(@Valid @RequestBody PaymentDetailsRequest paymentDetailsRequest, BindingResult result, HttpServletRequest request) throws Exception {
        // Check for validation errors
        if (result.hasErrors()) {
            // Here, we're just grabbing the first error, but you might want to send all of them.
            ObjectError error = result.getAllErrors().get(0);
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error.getDefaultMessage())
            );
        }
        try {
            if (!paymentDetailsRequest.getTransactionStatus().equals("success")) {
                paymentDetailsRequest.setTrees(new BigDecimal(0));
                paymentDetailsRequest.setLeafs(new BigDecimal(0));
            }

            // If any of trees or leaf is null then init it with 0.
            if (paymentDetailsRequest.getTrees() == null) {
                paymentDetailsRequest.setTrees(new BigDecimal(0));
            }
            if (paymentDetailsRequest.getLeafs() == null) {
                paymentDetailsRequest.setLeafs(new BigDecimal(0));
            }

            //Set userId from token
            String userId = authHelper.getUserId();

            if (userId.equals(paymentDetailsRequest.getUserId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "UserId provided in the payload does not match with the userId embedded in token"), null));
            }

            String charge = paymentService.chargeCustomer(
                    paymentDetailsRequest.getUserId(),
                    paymentDetailsRequest.getTrees(),
                    paymentDetailsRequest.getLeafs(),
                    paymentDetailsRequest.getPurchasedDate(),
                    paymentDetailsRequest.getTransactionId(),
                    paymentDetailsRequest.getTransactionStatus(),
                    paymentDetailsRequest.getAmount(),
                    paymentDetailsRequest.getPaymentMethod(),
                    paymentDetailsRequest.getCurrency(),
                    paymentDetailsRequest.getDescription(),
                    paymentDetailsRequest.getIpAddress()
            );

            return ResponseEntity.ok().body(new GenericStringResponse(null, charge));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.CHARGE, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    @PostMapping("/startPaymentToBuyTrees")
    public ResponseEntity<?> startPaymentToBuyTreesController(@Valid @RequestBody PaymentInitFromBackendRequest paymentInitFromBackend) {
        try {
            StripeClientResponse stripeClientResponse = stripeClient.createStripeSession(paymentInitFromBackend.getProductId(), paymentInitFromBackend.getSuccessUrl(), paymentInitFromBackend.getFailureUrl());
            String trees = stripeClientResponse.getProduct().getMetadata().get("trees");

            PaymentDetailsRequest paymentDetailsRequest = new PaymentDetailsRequest();
            paymentDetailsRequest.setTrees(new BigDecimal(Integer.parseInt(trees)));
            paymentDetailsRequest.setAmount(new BigDecimal(stripeClientResponse.getSession().getAmountTotal()));
            paymentDetailsRequest.setPurchasedDate(LocalDateTime.now());
            paymentDetailsRequest.setPaymentMethod(String.join(", ", stripeClientResponse.getSession().getPaymentMethodTypes()));
            paymentDetailsRequest.setCurrency(stripeClientResponse.getSession().getCurrency());
            paymentDetailsRequest.setDescription("Started payment to buy " + trees + " trees");
            paymentDetailsRequest.setTransactionId(stripeClientResponse.getSession().getId());

            String response = paymentService.startPaymentToBuyTrees(paymentDetailsRequest);

            return ResponseEntity.ok().body(new GenericStringResponse(null, stripeClientResponse.getSession().getUrl()));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.START_PAYMENT, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }

    }

    @PostMapping("/v2/charge")
    public ResponseEntity<?> chargeCardV2(@Valid @RequestBody PaymentDetailsRequest paymentDetailsRequest, BindingResult result, HttpServletRequest request) throws Exception {
        // Check for validation errors
        if (result.hasErrors()) {
            // Here, we're just grabbing the first error, but you might want to send all of them.
            ObjectError error = result.getAllErrors().get(0);
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error.getDefaultMessage())
            );
        }
        try {
            if (!paymentDetailsRequest.getTransactionStatus().equals("success")) {
                paymentDetailsRequest.setTrees(new BigDecimal(0));
                paymentDetailsRequest.setLeafs(new BigDecimal(0));
            }

            // If any of trees or leaf is null then init it with 0.
            if (paymentDetailsRequest.getTrees() == null) {
                paymentDetailsRequest.setTrees(new BigDecimal(0));
            }
            if (paymentDetailsRequest.getLeafs() == null) {
                paymentDetailsRequest.setLeafs(new BigDecimal(0));
            }

            //Set userId from token
            String userId = authHelper.getUserId();

            pdLogger.logInfo("BUY_TREES", "User : " + userId + " ,started payment to buy " + paymentDetailsRequest.getTrees() + " trees");

            String charge = paymentService.chargeCustomer(
                    userId,
                    paymentDetailsRequest.getTrees(),
                    paymentDetailsRequest.getLeafs(),
                    paymentDetailsRequest.getPurchasedDate(),
                    paymentDetailsRequest.getTransactionId(),
                    paymentDetailsRequest.getTransactionStatus(),
                    paymentDetailsRequest.getAmount(),
                    paymentDetailsRequest.getPaymentMethod(),
                    paymentDetailsRequest.getCurrency(),
                    paymentDetailsRequest.getDescription(),
                    paymentDetailsRequest.getIpAddress()
            );
            pdLogger.logInfo("BUY_TREES", "User : " + userId + " ,completed payment to buy " + paymentDetailsRequest.getTrees() + " trees");
            return ResponseEntity.ok().body(new GenericStringResponse(null, charge));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.CHARGE, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    @PostMapping("/clearPendingAndStalePayments/{days}")
    ResponseEntity<?> clearPendingPayments(@PathVariable Long days) {
        if (days == null || days == 0 || days < 0) {
            days = 14L;
        }
        try {
            List<ClearPendingAndStalePaymentsResponse> responses = paymentService.clearPendingAndStalePayments(days);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, responses));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping("/paymentsFailedInitiallyButSucceededLater")
    List<String> getPaymentsFailedInitiallyButSucceededLater() {
        try {
            return paymentService.clearPaymentWhichFailedInitiallyButSucceededLater();
        } catch (Exception e) {
            return null;
        }
    }


    @PostMapping("/buyLeafs")
    ResponseEntity<?> buyLeafs(@Valid @RequestBody BuyLeafsRequest buyLeafsRequest) {
        try {
            if (paymentService.checkIfTxnIdExists(buyLeafsRequest.getPurchaseToken())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Transaction Id already present in DB"), null));
            } else {
                pdLogger.logInfo("BUY_LEAFS", "Starting the buy leafs workflow");
                ProductPurchase productPurchase = appPaymentInitializer.getProductPurchase(buyLeafsRequest.getProductId(), buyLeafsRequest.getPurchaseToken());
                InAppProduct inAppProduct = appPaymentInitializer.getInAppProduct(buyLeafsRequest.getProductId());

                // check if purchase is complete; 0: purchased successfully, 1: canceled, 2: pending
                if (productPurchase.getPurchaseState() == 0) {

                    int purchaseLeaves = 0;
                    String productId = buyLeafsRequest.getProductId();

                    // If any of trees or leaf is null then init it with 0.
                    if (productId != null && productId.contains("_")) {
                        purchaseLeaves = Integer.parseInt(productId.substring(productId.indexOf("_") + 1));
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Product Id is not valid, cannot fetch leafs to add from productId"), null));
                    }

                    String userId = authHelper.getUserId();
                    String txnId = buyLeafsRequest.getPurchaseToken();
                    String paymentMethod = "Google_Play_Store";
                    String currency = inAppProduct.getDefaultPrice().get("currency").toString();
                    String amountInCents = inAppProduct.getDefaultPrice().get("priceMicros").toString();


                    String message = paymentService.completePaymentToBuyLeafs(
                            userId,
                            new BigDecimal(0),
                            new BigDecimal(purchaseLeaves),
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(productPurchase.getPurchaseTimeMillis()), ZoneId.systemDefault()),
                            txnId,
                            TransactionType.PAYMENT_COMPLETED.getDisplayName(),
                            new BigDecimal(amountInCents),
                            paymentMethod,
                            currency,
                            "Added " + purchaseLeaves + " leafs successfully for user.",
                            null
                    );
                    return ResponseEntity.ok().body(new GenericStringResponse(null, message));
                } else {
                    return ResponseEntity.ok().body(new GenericStringResponse(null, "Cannot add leafs to the user's wallet as the purchase state is not completed"));
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping("/getIosTransactionDetails")
    ResponseEntity<?> getIosTransactionDetails(@RequestBody BuyLeafsiOSRequest buyLeafsRequest) {
        try {
            TransactionDetails transactionDetailsObj = iosPaymentInitializer.getLeafsToAdd(buyLeafsRequest.getAppReceiptId());
            return ResponseEntity.ok().body(new GenericClassResponse<>(null, transactionDetailsObj));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericClassResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping("/getIosAppStoreConnectToken")
    ResponseEntity<?> getIosAppStoreConnectToken() {
        try {
            String token = iosPaymentInitializer.generateTokenForAppStoreConnect();
            return ResponseEntity.ok().body(new GenericStringResponse(null, token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @PostMapping("/buyLeafsIOS")
    ResponseEntity<?> buyLeafsIOS(@Valid @RequestBody BuyLeafsiOSRequest buyLeafsRequest) {
        try {

            pdLogger.logInfo("BUY_LEAFS", "Starting the buy leafs workflow for iOS");
            TransactionDetails txnDetails = iosPaymentInitializer.getLeafsToAdd(buyLeafsRequest.getAppReceiptId());

            if (paymentService.checkIfTxnIdExists(txnDetails.getTransactionId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Transaction Id already present in DB"), null));
            } else {
                BigDecimal purchaseLeaves = txnDetails.getLeafs();
                String userId = authHelper.getUserId();
                String txnId = txnDetails.getTransactionId();
                String paymentMethod = "IOS_Store";
                String currency = txnDetails.getCurrency();
                BigDecimal amountInCents = txnDetails.getPrice();


                String message = paymentService.completePaymentToBuyLeafs(
                        userId,
                        new BigDecimal(0),
                        purchaseLeaves,
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(txnDetails.getOriginalPurchaseDate()), ZoneId.systemDefault()),
                        txnId,
                        TransactionType.PAYMENT_COMPLETED.getDisplayName(),
                        amountInCents,
                        paymentMethod,
                        currency,
                        "Added " + purchaseLeaves + " leafs successfully for user.",
                        null
                );
                return ResponseEntity.ok().body(new GenericStringResponse(null, message));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
