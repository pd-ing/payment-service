package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InvalidTransactionIDException;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.payload.request.PaymentDetailsRequest;
import com.pding.paymentservice.payload.response.GenericStringResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.stripe.StripeClient;
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

    @Autowired
    PdLogger pdLogger;

    @Transactional
    public String chargeCustomer(String userId,
                                 BigDecimal purchasedTrees, BigDecimal purchasedLeafs, LocalDateTime purchasedDate,
                                 String transactionID, String transactionStatus, BigDecimal amount,
                                 String paymentMethod, String currency,
                                 String description, String ipAddress) throws Exception {
        try {
            validatePaymentIntentID(transactionID, userId);

            Wallet wallet = walletService.updateWalletForUser(userId, purchasedTrees, purchasedLeafs, purchasedDate);

            walletHistoryService.createWalletHistoryEntry(wallet.getId(), userId, purchasedTrees, purchasedLeafs, purchasedDate, transactionID, transactionStatus,
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
                paymentDetailsRequest.setLeafs(new BigDecimal(0));
            }

            // If any of trees or leaf is null then init it with 0.
            if (paymentDetailsRequest.getTrees() == null) {
                paymentDetailsRequest.setTrees(new BigDecimal(0));
            }
            if (paymentDetailsRequest.getLeafs() == null) {
                paymentDetailsRequest.setLeafs(new BigDecimal(0));
            }

            String charge = chargeCustomer(
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
}
