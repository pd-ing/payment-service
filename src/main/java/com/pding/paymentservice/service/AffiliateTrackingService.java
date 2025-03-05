package com.pding.paymentservice.service;

import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.repository.WalletHistoryRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@Log4j2
public class AffiliateTrackingService {

    @Autowired
    private UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    private WalletHistoryRepository walletHistoryRepository;

//    @Async
    public void trackTreePurchase(String userId, WalletHistory walletHistory) {
        if (userId == null || userId.isEmpty()) return;

        BigDecimal amount = walletHistory.getAmount();
        BigDecimal amountInDollars = walletHistory.getPaymentMethod().equals("PAYPAL") ? amount :
            amount.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

        try {
            List<WalletHistory> walletHistories = walletHistoryRepository.findByUserIdAndTransactionStatusIn(userId, Arrays.asList("COMPLETED", "success", "paymentCompleted"));
            walletHistory.setIsFirstPurchase(walletHistories.size() == 1);
            walletHistoryRepository.save(walletHistory);
            if (walletHistories.size() == 1) {
                userServiceNetworkManager.saveAffiliateTracking(userId, "FIRST_PURCHASE", walletHistory.getPurchasedTrees(), amountInDollars);
                return;
            }

            WalletHistory firstPurchase = walletHistories.stream()
                .filter(wl -> wl.getId() != walletHistory.getId())
                .min(Comparator.comparing(WalletHistory::getPurchaseDate)).orElse(null);

            if (firstPurchase == null) {
                return;
            }

            if (walletHistory.getPurchaseDate().isBefore(firstPurchase.getPurchaseDate().plusHours(24))) {
                userServiceNetworkManager.saveAffiliateTracking(userId, "FIRST_PURCHASE", walletHistory.getPurchasedTrees(), amountInDollars);
            } else {
                userServiceNetworkManager.saveAffiliateTracking(userId, "REPEAT_PURCHASE", walletHistory.getPurchasedTrees(), amountInDollars);
            }
        } catch (Exception e) {
            log.error("Failed to track tree purchase for user: {}", userId, e);
        }
    }
}
