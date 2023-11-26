package com.pding.paymentservice.service;

import com.pding.paymentservice.exception.ChargeNewCardException;
import com.pding.paymentservice.exception.InvalidTransactionIDException;
import com.pding.paymentservice.models.TransactionType;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.payload.request.PaymentDetailsRequest;
import com.pding.paymentservice.payload.response.ChargeResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.stripe.StripeClient;
import com.stripe.model.Charge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private StripeClient stripeClient;

    @Autowired
    WalletService walletService;

    @Autowired
    WalletHistoryService walletHistoryService;

    @Autowired
    LedgerService ledgerService;

    @Transactional
    public String chargeCustomer(String userId,
                                 BigDecimal purchasedTrees, LocalDateTime purchasedDate,
                                 String transactionID, String transactionStatus, BigDecimal amount,
                                 String paymentMethod, String currency,
                                 String description, String ipAddress) throws Exception {
        try {
            validatePaymentIntentID(transactionID, userId);
            
            Wallet wallet = walletService.updateWalletForUser(userId, purchasedTrees, purchasedDate);

            walletHistoryService.createWalletHistoryEntry(wallet.getId(), userId, purchasedTrees, purchasedDate, transactionID, transactionStatus,
                    amount, paymentMethod, currency, description, ipAddress);

            ledgerService.saveToLedger(wallet.getId(), purchasedTrees, TransactionType.TREE_PURCHASE);

            return "Payment Details updated successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new Exception("Payment Details update failed with following error : " + e.getMessage());
        }
    }


    private boolean validatePaymentIntentID(String transactionId, String userId) throws Exception {
        if (!stripeClient.isPaymentIntentIDPresentInStripe(transactionId)) {
            throw new InvalidTransactionIDException("paymentIntent id : " + transactionId + " , is invalid");
        }

        if (walletHistoryService.findByTransactionIdAndUserId(transactionId, userId).isPresent()) {
            throw new InvalidTransactionIDException("paymentIntent id : " + transactionId + " , is already used for the payment");
        }

        return true;
    }


    public ResponseEntity<?> chargeCustomer(PaymentDetailsRequest paymentDetailsRequest) {
        try {
            if (!paymentDetailsRequest.getTransactionStatus().equals("success")) {
                paymentDetailsRequest.setTrees(new BigDecimal(0));
            }
            String charge = chargeCustomer(paymentDetailsRequest.getUserId(), paymentDetailsRequest.getTrees(),
                    paymentDetailsRequest.getPurchasedDate(), paymentDetailsRequest.getTransactionId(),
                    paymentDetailsRequest.getTransactionStatus(), paymentDetailsRequest.getAmount(),
                    paymentDetailsRequest.getPaymentMethod(), paymentDetailsRequest.getCurrency(),
                    paymentDetailsRequest.getDescription(), paymentDetailsRequest.getIpAddress());

            return ResponseEntity.ok().body(new ChargeResponse(null, charge));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ChargeResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }
}
