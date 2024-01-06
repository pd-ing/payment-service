package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientLeafsException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.CallDetails;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.response.DonationResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GenericStringResponse;
import com.pding.paymentservice.repository.CallRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CallChargeService {
    @Autowired
    CallRepository callRepository;

    @Autowired
    WalletService walletService;

    @Autowired
    EarningService earningService;

    @Autowired
    LedgerService ledgerService;

    @Autowired
    UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    TokenSigner tokenSigner;

    @Autowired
    PdLogger pdLogger;


    @Transactional
    String CreateCallTransaction(String userId, String pddUserId, BigDecimal leafsToCharge, TransactionType callType) {
        walletService.deductLeafsFromWallet(userId, leafsToCharge);

        CallDetails callDetails = new CallDetails(userId, pddUserId, leafsToCharge, callType);
        callRepository.save(callDetails);

        earningService.addLeafsToEarning(pddUserId, leafsToCharge);
        ledgerService.saveToLedger(callDetails.getId(), new BigDecimal(0), leafsToCharge, callType);

        return "Leafs charge was successful";
    }

    public ResponseEntity<?> buyCall(String userId, String pdUserId, BigDecimal leafsToCharge, String callType) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userId parameter is required."), null));
        }

        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."), null));
        }

        if (leafsToCharge == null) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafsToCharge parameter is required."), null));
        }

        if ((leafsToCharge.compareTo(BigDecimal.ZERO) == 0) || (leafsToCharge.compareTo(BigDecimal.ZERO) < 0)) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafsToCharge should be greater than 0"), null));
        }

        TransactionType transactionType;
        if (callType.equals("audio")) {
            transactionType = TransactionType.AUDIO_CALL;
        } else if (callType.equals("video")) {
            transactionType = TransactionType.VIDEO_CALL;
        } else {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid callType passed,Valid callTyle is audio or video"), null));
        }

        try {
            String message = CreateCallTransaction(userId, pdUserId, leafsToCharge, transactionType);
            return ResponseEntity.ok().body(new GenericStringResponse(null, message));
        } catch (WalletNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        } catch (InsufficientLeafsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DonationResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DonationResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.CALL_CHARGE, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }
}
