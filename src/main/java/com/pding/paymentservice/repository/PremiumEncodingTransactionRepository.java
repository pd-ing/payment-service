package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.PremiumEncodingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for premium encoding fee transactions
 */
@Repository
public interface PremiumEncodingTransactionRepository extends JpaRepository<PremiumEncodingTransaction, Long> {

    /**
     * Find all transactions for a specific user
     *
     * @param userId The user ID
     * @return List of transactions
     */
    List<PremiumEncodingTransaction> findByUserId(String userId);

    /**
     * Find all transactions for a specific video
     *
     * @param videoId The video ID
     * @return List of transactions
     */
    List<PremiumEncodingTransaction> findByVideoId(String videoId);

    /**
     * Find all transactions for a specific user and video
     *
     * @param userId The user ID
     * @param videoId The video ID
     * @return List of transactions
     */
    List<PremiumEncodingTransaction> findByUserIdAndVideoId(String userId, String videoId);
}
