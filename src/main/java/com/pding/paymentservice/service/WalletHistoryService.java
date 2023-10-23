package com.pding.paymentservice.service;

import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.repository.WalletHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WalletHistoryService {

    @Autowired
    WalletHistoryRepository walletHistoryRepository;

    public void recordPurchaseHistory(long walletID, long userID, BigDecimal purchasedTrees,
                                      LocalDateTime purchasedDate,
                                      String transactionID, String transactionStatus, BigDecimal amount, String paymentMethod,
                                      String currency, String description, String ipAddress) {
        WalletHistory walletHistory = new WalletHistory(walletID, userID, purchasedTrees, purchasedDate, transactionID, transactionStatus,
                amount, paymentMethod, currency, description, ipAddress);
        walletHistoryRepository.save(walletHistory);
    }

    public List<WalletHistory> fetchWalletHistoryByWalletID(long walletID) {
        return walletHistoryRepository.findByWalletId(walletID);
    }

    public List<WalletHistory> fetchWalletHistoryByUserID(long userID) {
        return walletHistoryRepository.findByWalletId(userID);
    }

    public Optional<WalletHistory> findByTransactionIdAndUserId(String transactionID, long useriID) {
        return walletHistoryRepository.findByTransactionIdAndUserId(transactionID, useriID);
    }

}
