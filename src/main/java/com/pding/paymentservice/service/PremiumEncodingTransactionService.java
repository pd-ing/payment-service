package com.pding.paymentservice.service;

import com.pding.paymentservice.models.PremiumEncodingTransaction;
import com.pding.paymentservice.repository.PremiumEncodingTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing premium encoding fee transactions
 */
@Service
@Slf4j
public class PremiumEncodingTransactionService {

    @Autowired
    private PremiumEncodingTransactionRepository premiumEncodingTransactionRepository;

    /**
     * Create a new premium encoding fee transaction
     *
     * @param userId The user ID
     * @param videoId The video ID
     * @param fee The fee amount
     * @param status The transaction status
     * @param description The transaction description
     * @return The created transaction
     */
    @Transactional
    public PremiumEncodingTransaction createTransaction(String userId, String videoId, BigDecimal fee, String status, String description) {
        log.info("Creating premium encoding fee transaction for userId: {}, videoId: {}, fee: {}", userId, videoId, fee);

        PremiumEncodingTransaction transaction = PremiumEncodingTransaction.builder()
                .userId(userId)
                .videoId(videoId)
                .fee(fee)
                .transactionDate(LocalDateTime.now())
                .status(status)
                .description(description)
                .build();

        return premiumEncodingTransactionRepository.save(transaction);
    }

    /**
     * Get all transactions for a specific user
     *
     * @param userId The user ID
     * @return List of transactions
     */
    public List<PremiumEncodingTransaction> getTransactionsByUserId(String userId) {
        return premiumEncodingTransactionRepository.findByUserId(userId);
    }

    /**
     * Get all transactions for a specific video
     *
     * @param videoId The video ID
     * @return List of transactions
     */
    public List<PremiumEncodingTransaction> getTransactionsByVideoId(String videoId) {
        return premiumEncodingTransactionRepository.findByVideoId(videoId);
    }

    /**
     * Get all transactions for a specific user and video
     *
     * @param userId The user ID
     * @param videoId The video ID
     * @return List of transactions
     */
    public List<PremiumEncodingTransaction> getTransactionsByUserIdAndVideoId(String userId, String videoId) {
        return premiumEncodingTransactionRepository.findByUserIdAndVideoId(userId, videoId);
    }
}
