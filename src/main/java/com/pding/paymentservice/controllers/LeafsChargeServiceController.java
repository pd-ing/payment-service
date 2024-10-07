package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientLeafsException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.MessagePurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.CallPurchaseService;
import com.pding.paymentservice.service.FcmService;
import com.pding.paymentservice.service.MessagePurchaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
@Slf4j
public class LeafsChargeServiceController {

    @Autowired
    CallPurchaseService callPurchaseService;

    @Autowired
    MessagePurchaseService messagePurchaseService;

    @Autowired
    AuthHelper authHelper;

    @Autowired
    PdLogger pdLogger;


    @PostMapping(value = "/buyCallOrMessage")
    public ResponseEntity<?> buyCall(@RequestParam(value = "pdUserId") String pdUserId,
                                     @RequestParam(value = "leafsToCharge") BigDecimal leafsToCharge,
                                     @RequestParam(value = "treesToCharge", required = false, defaultValue = "0") BigDecimal treesToCharge,
                                     @RequestParam(value = "callType") String callType,
                                     @RequestParam(value = "callOrMessageId") String callOrMessageId,
                                     @RequestParam(value = "giftId", required = false) String giftId,
                                     @RequestParam(value = "notifyPd", required = false, defaultValue = "false") Boolean notifyPd
    ) {
        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."), null));
        }

        if (leafsToCharge == null) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafsToCharge parameter is required."), null));
        }

        if (callOrMessageId == null || callOrMessageId.isEmpty()) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "callOrMessageId parameter is required."), null));
        }

        if(leafsToCharge.compareTo(BigDecimal.ZERO) == 0 && treesToCharge.compareTo(BigDecimal.ZERO) == 0){
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafsToCharge or treesToCharge should be greater than 0"), null));
        }

        if ((treesToCharge.compareTo(BigDecimal.ZERO) < 0) || (leafsToCharge.compareTo(BigDecimal.ZERO) < 0)) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafsToCharge should be greater than 0"), null));
        }

        TransactionType transactionType;
        if (callType.equals("audio")) {
            transactionType = TransactionType.AUDIO_CALL;
        } else if (callType.equals("video")) {
            transactionType = TransactionType.VIDEO_CALL;
        } else if (callType.equals("message")) {
            transactionType = TransactionType.TEXT_MESSAGE;
        } else {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid callType passed,Valid callTyle is audio or video"), null));
        }

        if (giftId != null && !giftId.isEmpty()) {
            notifyPd = true;
        } else {
            notifyPd = false;
        }

        try {
//            pdLogger.logInfo("GIFT_TEST", "buyCallOrMessage API HIT, giftId : " + giftId + " , leafsToCharge : " + leafsToCharge);
            String userId = authHelper.getUserId();
            String message = "";

            if (transactionType.equals(TransactionType.AUDIO_CALL) || transactionType.equals(TransactionType.VIDEO_CALL)) {
                message = callPurchaseService.CreateCallTransaction(userId, pdUserId, leafsToCharge, transactionType, callOrMessageId, giftId, notifyPd);
            }

            if (transactionType.equals(TransactionType.TEXT_MESSAGE)) {
                boolean isGift = giftId != null && !giftId.isEmpty();
                message = messagePurchaseService.CreateMessageTransaction(userId, pdUserId, leafsToCharge, treesToCharge, callOrMessageId, isGift, giftId, notifyPd);
            }

            return ResponseEntity.ok().body(new GenericStringResponse(null, message));
        } catch (WalletNotFoundException e) {
            pdLogger.logException(PdLogger.EVENT.CALL_CHARGE, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        } catch (InsufficientLeafsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.CALL_CHARGE, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    @GetMapping(value = "/addBuyCallOrMessageEntryInRealTimeDb")
    public ResponseEntity<?> addBuyCallOrMessageEntryInRealTimeDb(@RequestParam(value = "callId") String callId) {
        callPurchaseService.addCallTransactionEntryToRealTimeDatabase(callId);
        return ResponseEntity.ok().body(new GenericStringResponse(null, "DONE"));
    }

    @GetMapping(value = "/callHistoryForPd")
    public ResponseEntity<?> getCallHistoryForPd(@RequestParam(value = "pdUserId", required = false) String pdUserId) {
        return callPurchaseService.callDetailsHistoryForPd(authHelper.getUserId());
    }


    @GetMapping(value = "/callHistoryForUser")
    public ResponseEntity<?> getCallHistoryForUser(@RequestParam(value = "userId", required = false) String userId) {
        return callPurchaseService.callDetailsHistoryForUser(authHelper.getUserId());
    }


    @GetMapping(value = "/topCallersForPd")
    public ResponseEntity<?> getTopCallersForPd(@RequestParam(value = "pdUserId", required = false) String pdUserId, @RequestParam(value = "limit") Long limit) {
        return callPurchaseService.getTopCallersForPd(authHelper.getUserId(), limit);
    }

    @GetMapping(value = "/topCallers")
    public ResponseEntity<?> getTopCallers(@RequestParam(value = "userId") String userId, @RequestParam(value = "limit") Long limit) {
        return callPurchaseService.getTopCallers(userId, limit);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
