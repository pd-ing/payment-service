package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InvalidTransactionIDException;
import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.payload.request.PaymentDetailsRequest;
import com.pding.paymentservice.payload.response.ClearPendingAndStalePaymentsResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.repository.WalletHistoryRepository;
import com.pding.paymentservice.repository.WalletRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.stripe.StripeClient;
import com.stripe.model.checkout.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private StripeClient stripeClient;

    @Autowired
    WalletService walletService;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    WalletHistoryService walletHistoryService;
    @Autowired
    WalletHistoryRepository walletHistoryRepository;

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

            ledgerService.saveToLedger(wallet.getId(), purchasedTrees, new BigDecimal(0), TransactionType.TREE_PURCHASE, userId);

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

            String transactionStatus = TransactionType.PAYMENT_STARTED.getDisplayName();
            walletHistoryService.createWalletHistoryEntry(wallet.getId(), userId, paymentDetailsRequest.getTrees(), new BigDecimal(0), paymentDetailsRequest.getPurchasedDate(), paymentDetailsRequest.getTransactionId(), transactionStatus,
                    paymentDetailsRequest.getAmount(), paymentDetailsRequest.getPaymentMethod(), paymentDetailsRequest.getCurrency(), paymentDetailsRequest.getDescription(), paymentDetailsRequest.getIpAddress());

            ledgerService.saveToLedger(wallet.getId(), paymentDetailsRequest.getTrees(), new BigDecimal(0), TransactionType.PAYMENT_STARTED, userId);

            return "Payment started successfully for paymentIntentId " + paymentDetailsRequest.getTransactionId();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new Exception("Payment starting failed with following error : " + e.getMessage());
        }
    }

    @Transactional
    public String completePaymentToBuyTrees(String paymentIntentId, String sessionId) throws Exception {
        Optional<WalletHistory> walletHistoryOptional = walletHistoryService.findByTransactionId(sessionId);

        if (walletHistoryOptional.isPresent()) {
            return completePaymentAndGiveTreesToUser(walletHistoryOptional.get(), paymentIntentId);
        } else {
            Optional<WalletHistory> walletHistoryOptionalUsingPaymentIntentId = walletHistoryService.findByTransactionId(paymentIntentId);

            if (walletHistoryOptionalUsingPaymentIntentId.isPresent()) {
                WalletHistory walletHistoryUsingPaymentIntentId = walletHistoryOptionalUsingPaymentIntentId.get();
                String stripePaymentStatus = stripeClient.checkPaymentStatus(paymentIntentId);

                // Payment was successful on stripe in 2nd attempt however in our backend we marked as paymentFailed in 1st attempt
                // This happens when user enter wrong cvv, in that case initially payment fails, webhook is called, and we mark payment as failed
                // However when user will enter correct cvv the same payment will pass, and then we need to change the state on our backend and give the trees to the user
                if (stripePaymentStatus.equals("succeeded") && walletHistoryUsingPaymentIntentId.getTransactionStatus().equals(TransactionType.PAYMENT_FAILED.getDisplayName())) {
                    return completePaymentAndGiveTreesToUser(walletHistoryUsingPaymentIntentId, paymentIntentId);
                } else if (stripePaymentStatus.equals("succeeded") && walletHistoryUsingPaymentIntentId.getTransactionStatus().equals(TransactionType.PAYMENT_COMPLETED.getDisplayName())) {
                    return "Payment is already completed and user had been given trees";
                } else if (stripePaymentStatus.equals("succeeded") && walletHistoryUsingPaymentIntentId.getTransactionStatus().equals("success")) {
                    return "Payment is already completed and user had been given trees, transactionStatus=" + walletHistoryUsingPaymentIntentId.getTransactionStatus();
                }
            }
            throw new Exception("Could not find wallet history information for the sessionId " + sessionId + " , or for the PaymentIntentId " + paymentIntentId);
        }
    }

    private String completePaymentAndGiveTreesToUser(WalletHistory walletHistory, String paymentIntentId) {
        if (walletHistory.getTransactionStatus().equals(TransactionType.PAYMENT_COMPLETED.getDisplayName())) {
            return "Payment is already completed for the paymentIntentId" + paymentIntentId;
        }

        Wallet wallet = walletService.updateWalletForUser(walletHistory.getUserId(), walletHistory.getPurchasedTrees(), new BigDecimal(0), walletHistory.getPurchaseDate());

        ledgerService.saveToLedger(wallet.getId(), walletHistory.getPurchasedTrees(), new BigDecimal(0), TransactionType.PAYMENT_COMPLETED, walletHistory.getUserId());

        String description = walletHistory.getDescription() + ", Completed payment successfully";
        walletHistory.setDescription(description);
        walletHistory.setTransactionId(paymentIntentId);
        walletHistory.setTransactionStatus(TransactionType.PAYMENT_COMPLETED.getDisplayName());
        walletHistoryService.save(walletHistory);
        return "Payment completed successfully for paymentIntentId " + paymentIntentId;
    }

    @Transactional
    public String failPaymentToBuyTrees(String paymentIntentId, String sessionId) throws Exception {
        Optional<WalletHistory> walletHistoryOptional = walletHistoryService.findByTransactionId(sessionId);

        if (walletHistoryOptional.isPresent()) {
            WalletHistory walletHistory = walletHistoryOptional.get();

            if (walletHistory.getTransactionStatus().equals(TransactionType.PAYMENT_FAILED.getDisplayName())) {
                return "Payment is already failed for the paymentIntentId" + paymentIntentId;
            }
            ledgerService.saveToLedger(walletHistory.getWalletId(), walletHistory.getPurchasedTrees(), new BigDecimal(0), TransactionType.PAYMENT_FAILED, walletHistory.getUserId());

            String description = walletHistory.getDescription() + ", payment failed";
            walletHistory.setDescription(description);
            walletHistory.setTransactionId(paymentIntentId);
            walletHistory.setTransactionStatus(TransactionType.PAYMENT_FAILED.getDisplayName());
            walletHistoryService.save(walletHistory);

            return "Payment failed for paymentIntentId " + paymentIntentId;
        } else {
            Optional<WalletHistory> walletHistoryOptionalUsingPaymentIntentId = walletHistoryService.findByTransactionId(paymentIntentId);

            if (walletHistoryOptionalUsingPaymentIntentId.isPresent()) {
                WalletHistory walletHistoryUsingPaymentIntentId = walletHistoryOptionalUsingPaymentIntentId.get();
                String stripePaymentStatus = stripeClient.checkPaymentStatus(paymentIntentId);

                if (!stripePaymentStatus.equals("succeeded") && walletHistoryUsingPaymentIntentId.getTransactionStatus().equals(TransactionType.PAYMENT_FAILED.getDisplayName())) {
                    return "Payment is already Failed and trees are not given to the user";
                } else if (stripePaymentStatus.equals("succeeded") && walletHistoryUsingPaymentIntentId.getTransactionStatus().equals(TransactionType.PAYMENT_COMPLETED.getDisplayName())) {
                    return "Payment is already completed and user had been given trees";
                }
            }
            throw new Exception("Could not find wallet history information for the sessionId " + sessionId);
        }
    }


    @Transactional
    public String completeRefundTrees(BigDecimal amountInCents, BigDecimal treesToRefund, String paymentIntentId) {
        Optional<WalletHistory> walletHistoryOptional = walletHistoryService.findByTransactionId(paymentIntentId);
        if (walletHistoryOptional.isPresent()) {
            WalletHistory walletHistory = walletHistoryOptional.get();

            Wallet userWallet = walletService.fetchWalletByUserId(walletHistory.getUserId()).get();

            if (userWallet.getTrees().compareTo(treesToRefund) < 0) {
                throw new RuntimeException("Cannot complete the refund request, because of insufficient tree balance, For userId :" + walletHistory.getUserId() +
                        ", treesInWallet:" + userWallet.getTrees() + " , refundRequestAmtInTrees:" + treesToRefund);
            }


            String transactionId = treesToRefund + "_trees_refunded_for_" + walletHistory.getTransactionId();
            Optional<WalletHistory> walletHistoryRefundEntryOptional = walletHistoryService.findByTransactionId(paymentIntentId);
            if (walletHistoryRefundEntryOptional.isPresent()) {
                String description = walletHistory.getDescription() + ", Refund completed successfully";
                walletHistory.setDescription(description);
                walletHistory.setTransactionStatus(TransactionType.REFUND_COMPLETED.getDisplayName());
                walletHistoryService.save(walletHistory);
            } else {
                walletHistoryService.createWalletHistoryEntry(walletHistory.getWalletId(),
                        walletHistory.getUserId(),
                        treesToRefund,
                        new BigDecimal(0),
                        LocalDateTime.now(),
                        transactionId,
                        TransactionType.REFUND_COMPLETED.getDisplayName(),
                        amountInCents,
                        "Refunded through stripe",
                        walletHistory.getCurrency(),
                        "Refund completed successfully",
                        walletHistory.getIpAddress()
                );
            }

            walletService.deductTreesFromWallet(walletHistory.getUserId(), treesToRefund);
            ledgerService.saveToLedger(walletHistory.getWalletId(), treesToRefund, new BigDecimal(0), TransactionType.REFUND_COMPLETED, walletHistory.getUserId());

            return "Refund completed successFully for the paymentIntentId : " + paymentIntentId + " , UserId :" + walletHistory.getUserId() + ", treesRefunded:" + treesToRefund;
        } else {
            throw new RuntimeException("Refund failed because record not found in wallet history for transactionId:" + paymentIntentId);
        }
    }


    @Transactional
    public String cancelRefundTrees(BigDecimal treeToAdd, String paymentIntentId) {
        Optional<WalletHistory> walletHistoryOptional = walletHistoryService.findByTransactionId(paymentIntentId);
        if (walletHistoryOptional.isPresent()) {
            WalletHistory walletHistory = walletHistoryOptional.get();

            String description = walletHistory.getDescription() + ", Refund cancelled";
            walletHistory.setDescription(description);
            walletHistory.setTransactionStatus(TransactionType.REFUND_CANCELLED.getDisplayName());
            walletHistoryService.save(walletHistory);

            walletService.addToWallet(walletHistory.getUserId(), treeToAdd, new BigDecimal(0), LocalDateTime.now());
            ledgerService.saveToLedger(walletHistory.getWalletId(), treeToAdd, new BigDecimal(0), TransactionType.REFUND_CANCELLED, walletHistory.getUserId());

            return "Refund completed successFully for the paymentIntentId : " + paymentIntentId + " , UserId :" + walletHistory.getUserId() + ", treesAddedBack:" + treeToAdd;
        } else {
            throw new RuntimeException("Cancelling Refund failed because record not found in wallet history for transactionId:" + paymentIntentId);
        }
    }

    //This API was used 1 time, when the case where payment initially fails and later succeeded cases was not handled
    @Transactional
    public List<String> clearPaymentWhichFailedInitiallyButSucceededLater() {
        List<String> result = new ArrayList<>();

        List<WalletHistory> walletHistoryList = walletHistoryRepository.findByTransactionStatus(TransactionType.PAYMENT_FAILED.getDisplayName());

        for (WalletHistory walletHistory : walletHistoryList) {
            String stripePaymentStatus = stripeClient.checkPaymentStatus(walletHistory.getTransactionStatus());
            if (stripePaymentStatus.equals("succeeded")) {
                result.add(clearPaymentAndGiveTress(walletHistory));
            }
        }

        return result;
    }

    // This API will clear payments where Users were charged money but trees were not given,
    // This API will also mark update the statuses of the payments which users started but never completed.
    @Transactional
    public List<ClearPendingAndStalePaymentsResponse> clearPendingAndStalePayments(Long days) throws Exception {
        List<ClearPendingAndStalePaymentsResponse> cpaspList = new ArrayList<>();
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        List<Object[]> objects = walletHistoryRepository.findPendingTransactions(startDate);

        for (Object innerObject : objects) {
            Object[] data = (Object[]) innerObject;
            String userId = data[0].toString();
            String emailId = data[1].toString();
            String sessionId = data[2].toString();
            String purchasedDate = data[3].toString();

            ClearPendingAndStalePaymentsResponse cpaspObj = new ClearPendingAndStalePaymentsResponse();
            cpaspObj.setUserId(userId);
            cpaspObj.setEmailId(emailId);
            cpaspObj.setSessionId(sessionId);
            cpaspObj.setPurchasedDate(purchasedDate);

            String result = "";

            Session session = null;
            try {
                session = stripeClient.getSessionDetails(sessionId);
                if (stripeClient.isSessionCompleteOrExpired(session)) {
                    if (stripeClient.isPaymentDone(session)) {
                        result = completePaymentToBuyTrees(session.getPaymentIntent(), session.getId());


                        cpaspObj.setPaymentIntentId(session.getPaymentIntent());
                        cpaspObj.setStripeSessionPaymentStatus(stripeClient.isPaymentDone(session));
                        cpaspObj.setBackendPaymentStatus(result);


                    } else {
                        // Passing sessionId as paymentIntentId as, whenever payment Fails at that time sessionId is not generated.
                        result = failPaymentToBuyTrees(session.getId(), session.getId());

                        cpaspObj.setPaymentIntentId(session.getPaymentIntent());
                        cpaspObj.setStripeSessionPaymentStatus(stripeClient.isPaymentDone(session));
                        cpaspObj.setBackendPaymentStatus(result);
                    }
                } else {
                    cpaspObj.setPaymentIntentId(session.getPaymentIntent());
                    cpaspObj.setStripeSessionPaymentStatus(stripeClient.isPaymentDone(session));
                    cpaspObj.setBackendPaymentStatus("Stripe session is not expired or completed yet, It means user has started the payment but not completed yet, If he dosent do the payment within 24hrs of the purchaseDate, then Session will expire and after that run this API again to update the status,If user completes the payment then No action will be needed");
                }
            } catch (Exception e) {
                cpaspObj.setPaymentIntentId("");
                cpaspObj.setStripeSessionPaymentStatus(false);
                cpaspObj.setBackendPaymentStatus("Cannot find Stripe session for sessionId " + sessionId + ", This means the payment was done using the OLD PAYMENT METHOD, where payment init was done from FE, NO ACTION NEEDED FOR THIS RECORD");
            }
            cpaspList.add(cpaspObj);
        }
        return cpaspList;
    }

    private String clearPaymentAndGiveTress(WalletHistory walletHistory) {
        String transactionId = walletHistory.getTransactionId();

        Optional<String> emailId = walletRepository.findEmailById(walletHistory.getUserId());

        String stripePaymentStatus = stripeClient.checkPaymentStatus(transactionId);

        if (stripePaymentStatus.equals("succeeded")) {
            String paymentUpdateStatus = "";
            try {
                paymentUpdateStatus = completePaymentToBuyTrees(transactionId, transactionId);
            } catch (Exception e) {
                paymentUpdateStatus = e.getMessage();
            }
            return "UserId:" + walletHistory.getUserId() + " , Email :" + emailId.get() + " , StripePaymentStatus :" + stripePaymentStatus + ", BackendPaymentStatus:" + walletHistory.getTransactionStatus() + " PaymentIntentId:" + transactionId + " , ClearPaymentStatus : " + paymentUpdateStatus;
        }

        return "UserId:" + walletHistory.getUserId() + " , Email :" + emailId.get() + " , StripePaymentStatus :" + stripePaymentStatus + ", BackendPaymentStatus:" + walletHistory.getTransactionStatus() + " PaymentIntentId:" + transactionId + " , ClearPaymentStatus : Trees were not given to user as paymentIntent status was not succeeded ";
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
