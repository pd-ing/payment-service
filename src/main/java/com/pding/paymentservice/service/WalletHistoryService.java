package com.pding.paymentservice.service;

import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.repository.WalletHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletHistoryService {

    @Autowired
    WalletHistoryRepository walletHistoryRepository;

    public void recordPurchaseHistory(long userID, String stripeCustomerID, long walletID, BigDecimal purchasedTrees) {
        WalletHistory walletHistory = new WalletHistory(userID, stripeCustomerID, walletID, purchasedTrees, LocalDateTime.now());
        walletHistoryRepository.save(walletHistory);
    }

    public List<WalletHistory> fetchWalletHistoryByWalletID(long walletID) {
        return walletHistoryRepository.findByWalletId(walletID);
    }

    public List<WalletHistory> fetchWalletHistoryByUserID(long userID) {
        return walletHistoryRepository.findByWalletId(userID);
    }
}
