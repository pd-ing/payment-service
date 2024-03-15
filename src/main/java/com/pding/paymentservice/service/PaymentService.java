package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InvalidTransactionIDException;
import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.payload.request.PaymentDetailsRequest;
import com.pding.paymentservice.payload.response.GenericStringResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.stripe.StripeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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

    @Autowired
    AuthHelper authHelper;

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

            ledgerService.saveToLedger(wallet.getId(), purchasedTrees, new BigDecimal(0), TransactionType.TREE_PURCHASE);

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

    @Transactional
    public String startPaymentToBuyTrees(PaymentDetailsRequest paymentDetailsRequest) throws Exception {

        try {
            String userId = authHelper.getUserId();
            Wallet wallet = walletService.fetchWalletByUserId(userId).get();

            if (!validatePaymentIntentId(paymentDetailsRequest.getTransactionId())) {
                throw new Exception("Invalid payment attempt made, As paymentIntentId is already used");
            }
            
            String transactionStatus = TransactionType.PAYMENT_STARTED.getDisplayName();
            walletHistoryService.createWalletHistoryEntry(wallet.getId(), userId, paymentDetailsRequest.getTrees(), paymentDetailsRequest.getLeafs(), paymentDetailsRequest.getPurchasedDate(), paymentDetailsRequest.getTransactionId(), transactionStatus,
                    paymentDetailsRequest.getAmount(), paymentDetailsRequest.getPaymentMethod(), paymentDetailsRequest.getCurrency(), paymentDetailsRequest.getDescription(), paymentDetailsRequest.getIpAddress());

            ledgerService.saveToLedger(wallet.getId(), paymentDetailsRequest.getTrees(), new BigDecimal(0), TransactionType.PAYMENT_STARTED);

            return "Payment started successfully for paymentIntentId " + paymentDetailsRequest.getTransactionId();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new Exception("Payment starting failed with following error : " + e.getMessage());
        }
    }

    @Transactional
    public String completePaymentToBuyTrees(String paymentIntentId) throws Exception {
        Optional<WalletHistory> walletHistoryOptional = walletHistoryService.findByTransactionId(paymentIntentId);

        if (walletHistoryOptional.isPresent()) {
            WalletHistory walletHistory = walletHistoryOptional.get();

            Wallet wallet = walletService.updateWalletForUser(walletHistory.getUserId(), walletHistory.getPurchasedTrees(), walletHistory.getPurchasedLeafs(), walletHistory.getPurchaseDate());

            ledgerService.saveToLedger(wallet.getId(), walletHistory.getPurchasedTrees(), new BigDecimal(0), TransactionType.PAYMENT_COMPLETED);

            walletHistory.setTransactionStatus(TransactionType.PAYMENT_COMPLETED.getDisplayName());

            walletHistoryService.save(walletHistory);
            return "Payment started successfully for paymentIntentId " + paymentIntentId;
        } else {
            throw new Exception("Could not find wallet history information for the paymentIntentId " + paymentIntentId);
        }
    }

    @Transactional
    public String failPaymentToBuyTrees(String paymentIntentId) throws Exception {
        Optional<WalletHistory> walletHistoryOptional = walletHistoryService.findByTransactionId(paymentIntentId);

        if (walletHistoryOptional.isPresent()) {
            WalletHistory walletHistory = walletHistoryOptional.get();

            ledgerService.saveToLedger(walletHistory.getWalletId(), walletHistory.getPurchasedTrees(), new BigDecimal(0), TransactionType.PAYMENT_FAILED);

            walletHistory.setTransactionStatus(TransactionType.PAYMENT_FAILED.getDisplayName());

            walletHistoryService.save(walletHistory);

            return "Payment failed for paymentIntentId " + paymentIntentId;
        } else {
            throw new Exception("Could not find wallet history information for the paymentIntentId " + paymentIntentId);
        }
    }

    private boolean validatePaymentIntentId(String paymentIntentId) {
        Optional<WalletHistory> walletHistory = walletHistoryService.findByTransactionId(paymentIntentId);

        if (walletHistory.isPresent()) {
            return false;
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

            //Set userId from token
            String userId = authHelper.getUserId();

            if (userId.equals(paymentDetailsRequest.getUserId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "UserId provided in the payload does not match with the userId embedded in token"), null));
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


    public ResponseEntity<?> chargeCustomerV2(PaymentDetailsRequest paymentDetailsRequest) {
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

            String charge = chargeCustomer(
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
}
