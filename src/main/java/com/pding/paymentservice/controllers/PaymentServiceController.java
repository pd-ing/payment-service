package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.request.PaymentDetailsRequest;
import com.pding.paymentservice.payload.request.PaymentInitFromBackendRequest;
import com.pding.paymentservice.payload.response.ClearPendingAndStalePaymentsResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.payload.response.MessageResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.service.PaymentService;
import com.pding.paymentservice.stripe.StripeClient;
import com.pding.paymentservice.stripe.StripeClientResponse;
import com.pding.paymentservice.util.FirebaseRealtimeDbHelper;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @GetMapping(value = "/test")
    public ResponseEntity<?> sampleGet() {
        return ResponseEntity.ok()
                .body(new MessageResponse("This is test API, With JWT Validation"));
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
        return paymentService.chargeCustomer(paymentDetailsRequest);
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
        return paymentService.chargeCustomerV2(paymentDetailsRequest);
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


    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
