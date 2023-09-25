package com.pding.paymentservice.repository;


import com.pding.paymentservice.models.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WalletHistoryRepository extends JpaRepository<WalletHistory, Long> {


    // Find wallet history entries by walletId
    List<WalletHistory> findByWalletId(Long walletId);

    // Find wallet history entries by purchaseDate range
    List<WalletHistory> findByPurchaseDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find wallet history entries by purchasedTrees greater than a specified value
    List<WalletHistory> findByPurchasedTreesGreaterThan(BigDecimal value);

    // Find the latest wallet history entry for a specific wallet
    WalletHistory findFirstByWalletIdOrderByPurchaseDateDesc(Long walletId);

    // Find wallet history entries by user ID (if walletId is associated with a user)
    List<WalletHistory> findByWalletIdIn(List<Long> walletIds);
}