package com.pding.paymentservice.service;

import com.pding.paymentservice.exception.ChargeNewCardException;
import com.pding.paymentservice.exception.InvalidTransactionIDException;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.stripe.StripeClient;
import com.stripe.model.Charge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional
    public String chargeCustomer(long userID,
                                 BigDecimal purchasedTrees, LocalDateTime purchasedDate,
                                 String transactionID, String transactionStatus, BigDecimal amount,
                                 String paymentMethod, String currency,
                                 String description, String ipAddress) {
        try {
            if (!stripeClient.isTransactionIdPresentInStripe(transactionID)) {
                throw new InvalidTransactionIDException("Transaction id : " + transactionID + " , is invalid or duplicate");
            }
            Wallet wallet = updateWalletForUser(userID, purchasedTrees, purchasedDate);
            createWalletHistoryEntry(wallet.getId(), userID, purchasedTrees, purchasedDate, transactionID, transactionStatus,
                    amount, paymentMethod, currency, description, ipAddress);
            return "Payment Details updated successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return "Payment Details update failed with following error : " + e.getMessage();
        }
    }

    private Wallet updateWalletForUser(Long userID, BigDecimal purchasedTrees, LocalDateTime purchasedDate) {
        Wallet wallet = walletService.addToWallet(userID, purchasedTrees, purchasedDate);
        log.info("Wallet table updated", wallet);
        return wallet;
    }

    private void createWalletHistoryEntry(long walletID, long userID,
                                          BigDecimal purchasedTrees, LocalDateTime purchasedDate,
                                          String transactionID, String transactionStatus, BigDecimal amount,
                                          String paymentMethod, String currency,
                                          String description, String ipAddress) {
        walletHistoryService.recordPurchaseHistory(walletID, userID, purchasedTrees, purchasedDate, transactionID, transactionStatus,
                amount, paymentMethod, currency, description, ipAddress);
        log.info("Wallet history table updated");
    }

}
