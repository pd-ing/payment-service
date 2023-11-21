package com.pding.paymentservice.service;

import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.WalletHistoryResponse;
import com.pding.paymentservice.repository.WalletHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class WalletHistoryService {

    @Autowired
    WalletHistoryRepository walletHistoryRepository;

    public void recordPurchaseHistory(long walletId, String userId, BigDecimal purchasedTrees,
                                      LocalDateTime purchasedDate,
                                      String transactionID, String transactionStatus, BigDecimal amount, String paymentMethod,
                                      String currency, String description, String ipAddress) {
        WalletHistory walletHistory = new WalletHistory(walletId, userId, purchasedTrees, purchasedDate, transactionID, transactionStatus,
                amount, paymentMethod, currency, description, ipAddress);
        walletHistoryRepository.save(walletHistory);
    }

    public List<WalletHistory> fetchWalletHistoryByWalletID(long walletID) {
        return walletHistoryRepository.findByWalletId(walletID);
    }

    public List<WalletHistory> fetchWalletHistoryByUserId(String userId) {
        return walletHistoryRepository.findByUserId(userId);
    }

    public Optional<WalletHistory> findByTransactionIdAndUserId(String transactionID, String userId) {
        return walletHistoryRepository.findByTransactionIdAndUserId(transactionID, userId);
    }

    public void createWalletHistoryEntry(long walletID, String userId,
                                         BigDecimal purchasedTrees, LocalDateTime purchasedDate,
                                         String transactionID, String transactionStatus, BigDecimal amount,
                                         String paymentMethod, String currency,
                                         String description, String ipAddress) {
        recordPurchaseHistory(walletID, userId, purchasedTrees, purchasedDate, transactionID, transactionStatus,
                amount, paymentMethod, currency, description, ipAddress);
        log.info("Wallet history table updated");
    }


    public ResponseEntity<?> getHistory(String userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            List<WalletHistory> walletHistory = fetchWalletHistoryByUserId(userId);
            return ResponseEntity.ok().body(new WalletHistoryResponse(null, walletHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WalletHistoryResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

}
