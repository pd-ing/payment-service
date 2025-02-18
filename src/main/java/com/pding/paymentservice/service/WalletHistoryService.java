package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.repository.WalletHistoryRepository;
import com.pding.paymentservice.repository.WalletRepository;
import com.pding.paymentservice.paymentclients.stripe.StripeClient;
import com.pding.paymentservice.util.LogSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class WalletHistoryService {

    @Autowired
    WalletHistoryRepository walletHistoryRepository;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    StripeClient stripeClient;


    @Autowired
    WalletRepository walletRepository;

    public void recordPurchaseHistory(String walletId, String userId, BigDecimal purchasedTrees, BigDecimal purchasedLeafs,
                                      LocalDateTime purchasedDate,
                                      String transactionID, String transactionStatus, BigDecimal amount, String paymentMethod,
                                      String currency, String description, String ipAddress) {
        WalletHistory walletHistory = new WalletHistory(walletId, userId, purchasedTrees, purchasedLeafs, purchasedDate, transactionID, transactionStatus,
                amount, paymentMethod, currency, description, ipAddress);
        walletHistoryRepository.save(walletHistory);
        log.info("Wallet history saved for walletID: {}", LogSanitizer.sanitizeForLog(walletId));
    }

    public void recordRefundHistory(String walletId, String userId, BigDecimal purchasedTrees, BigDecimal purchasedLeafs,
                                      LocalDateTime purchasedDate,
                                      String transactionID, String transactionStatus, BigDecimal amount, String paymentMethod,
                                      String currency, String description, String ipAddress, String refundId) {
        WalletHistory walletHistory = new WalletHistory(walletId, userId, purchasedTrees, purchasedLeafs, purchasedDate, transactionID, transactionStatus,
                amount, paymentMethod, currency, description, ipAddress, refundId);
        walletHistoryRepository.save(walletHistory);
        log.info("Wallet history saved for walletID: {}", LogSanitizer.sanitizeForLog(walletId));
    }

    public List<WalletHistory> fetchWalletHistoryByWalletID(String walletId) {
        return walletHistoryRepository.findByWalletId(walletId);
    }

    public Page<WalletHistory> fetchWalletHistoryByUserId(String userId, int page, int size) {
        List<String> statuses = Arrays.asList("paymentCompleted", "success");
        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseDate").descending());
        return walletHistoryRepository.findByUserIdAndTransactionStatusIn(userId, statuses, pageable);
    }


    public Page<WalletHistory> fetchPurchasedLeafWalletHistoryByUserId(String userId, int page, int size, Boolean sortAsc) {
        List<String> statuses = Arrays.asList("paymentCompleted", "success");

        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseDate").descending());
        if (sortAsc)
            pageable = PageRequest.of(page, size, Sort.by("purchaseDate").ascending());

        return walletHistoryRepository.findAllWithPurchasedLeafsGreaterThanZeroAndStatusAndUserId(userId, statuses, pageable);
    }

    public Optional<WalletHistory> findByTransactionIdAndUserId(String transactionID, String userId) {
        return walletHistoryRepository.findByTransactionIdAndUserId(transactionID, userId);
    }

    public Optional<WalletHistory> findByTransactionId(String transactionId) {
        return walletHistoryRepository.findByTransactionId(transactionId);
    }

    public List<WalletHistory> findByTransactionIdWithPattern(String pattern) {
        return walletHistoryRepository.findByTransactionIdPattern(pattern);
    }

    public void createWalletHistoryEntry(String walletID, String userId,
                                         BigDecimal purchasedTrees, BigDecimal purchasedLeafs, LocalDateTime purchasedDate,
                                         String transactionID, String transactionStatus, BigDecimal amount,
                                         String paymentMethod, String currency,
                                         String description, String ipAddress) {
        recordPurchaseHistory(walletID, userId, purchasedTrees, purchasedLeafs, purchasedDate, transactionID, transactionStatus,
                amount, paymentMethod, currency, description, ipAddress);
    }

    public void createWalletHistoryEntry(String walletID, String userId,
                                         BigDecimal purchasedTrees, BigDecimal purchasedLeafs, LocalDateTime purchasedDate,
                                         String transactionID, String transactionStatus, BigDecimal amount,
                                         String paymentMethod, String currency,
                                         String description, String ipAddress, String refundId) {
        recordRefundHistory(walletID, userId, purchasedTrees, purchasedLeafs, purchasedDate, transactionID, transactionStatus,
                amount, paymentMethod, currency, description, ipAddress, refundId);
    }


    public void save(WalletHistory walletHistory) {
        walletHistoryRepository.save(walletHistory);
    }

    public Optional<WalletHistory> findByRefundId(String transactionId) {
        return walletHistoryRepository.findByRefundId(transactionId);
    }


}
