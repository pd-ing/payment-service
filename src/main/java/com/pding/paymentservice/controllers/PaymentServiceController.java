package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.request.ClearPendingPaymentRequest;
import com.pding.paymentservice.payload.request.PaymentDetailsRequest;
import com.pding.paymentservice.payload.request.PaymentInitFromBackendRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GenericStringResponse;
import com.pding.paymentservice.payload.response.MessageResponse;
import com.pding.paymentservice.service.PaymentService;
import com.pding.paymentservice.stripe.StripeClient;
import com.pding.paymentservice.stripe.StripeClientResponse;
import com.stripe.model.checkout.Session;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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


    @PostMapping("/clearPendingPayment")
    ResponseEntity<?> clearPendingPayments(@Valid @RequestBody ClearPendingPaymentRequest clearPendingPaymentRequest) {
        String sessionId = clearPendingPaymentRequest.getSessionId();
        try {
            Session session = stripeClient.getSessionDetails(sessionId);
            if (stripeClient.isSessionCompleteOrExpired(session)) {
                if (stripeClient.isPaymentDone(session)) {
                    paymentService.completePaymentToBuyTrees(session.getPaymentIntent(), session.getId());
                    return ResponseEntity.ok().body(new GenericStringResponse(null, "Payment marked as completed for sessionId:" + sessionId + " , PaymentIntentId:" + session.getPaymentIntent()));
                } else {
                    // Passing sessionId as paymentIntentId as, whenever payment Fails at that time sessionId is not generated.
                    paymentService.failPaymentToBuyTrees(session.getId(), session.getId());
                    return ResponseEntity.ok().body(new GenericStringResponse(null, "Payment marked as failed for sessionId:" + sessionId + " , PaymentIntentId:" + session.getPaymentIntent()));
                }
            }
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Did not updated the payment status as session is not expired or completed, sessionId:" + sessionId + " , PaymentIntentId:" + session.getPaymentIntent()));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Error occured while updating payment status for sessionId:" + sessionId));
        }
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
