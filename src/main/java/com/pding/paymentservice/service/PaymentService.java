package com.pding.paymentservice.service;

import com.pding.paymentservice.exception.ChargeNewCardException;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.stripe.StripeClient;
import com.stripe.model.Charge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    public String chargeCustomer(Long userID, String token, double amount) {
        try {
            Charge charge = stripeClient.chargeNewCard(token, amount);
            log.info("Payment successfully charged");
            if ("succeeded".equals(charge.getStatus())) {
                Wallet wallet = updateWalletForUser(userID, charge.getCustomer(), amount);
                createWalletHistoryEntry(userID, charge.getCustomer(), wallet.getId(), amount);
            } else {
                log.info("Payment failed", charge);
            }

            log.info("Charge details:", charge.toJson());

            return charge.toJson();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new ChargeNewCardException(e.getMessage());
        }
    }

    private Wallet updateWalletForUser(Long userID, String customerId, double amount) {
        // Perform the logic to update the user's wallet based on the payment amount
        // You can fetch the user's current wallet balance and add the purchased amount.
        // Then, update the wallet in the database.
        // Example: walletService.updateWalletBalance(customerId, amount);

        Wallet wallet = walletService.addToWallet(userID, customerId, new BigDecimal(amount));
        log.info("Wallet table updated", wallet);
        return wallet;
    }

    private void createWalletHistoryEntry(long userID, String stripeCustomerID, Long walletID, double amount) {
        // Create a wallet history entry to record the purchase
        // You can include details like the customer ID, purchased amount, and timestamp.
        // Then, save the wallet history entry in the database.
        // Example: walletHistoryService.createWalletHistoryEntry(customerId, amount);
        walletHistoryService.recordPurchaseHistory(userID, stripeCustomerID, walletID, new BigDecimal(amount));
        log.info("Wallet history table updated");
    }
}
