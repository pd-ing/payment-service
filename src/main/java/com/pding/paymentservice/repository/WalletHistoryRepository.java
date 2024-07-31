package com.pding.paymentservice.repository;


import com.pding.paymentservice.models.WalletHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletHistoryRepository extends JpaRepository<WalletHistory, String> {

    List<WalletHistory> findByWalletId(String walletId);

    List<WalletHistory> findByUserId(String userId);

    Optional<WalletHistory> findByTransactionIdAndUserId(String transactionId, String userId);

    Optional<WalletHistory> findByTransactionId(String transactionId);

    @Query("SELECT wh FROM WalletHistory wh WHERE wh.transactionId LIKE %:pattern%")
    List<WalletHistory> findByTransactionIdPattern(@Param("pattern") String pattern);

    @Query("SELECT wh FROM WalletHistory wh WHERE wh.transactionStatus = ?1")
    List<WalletHistory> findByTransactionStatus(String transactionStatus);

    Page<WalletHistory> findByUserIdAndTransactionStatusIn(String userId, List<String> statuses, Pageable pageable);

    @Query(value = "SELECT wh.user_id, u.email, wh.transaction_id, wh.purchase_date " +
            "FROM wallet_history wh " +
            "JOIN users u ON wh.user_id = u.id " +
            "WHERE wh.transaction_status = 'paymentStarted' " +
            "AND wh.purchase_date >= :startDate",
            nativeQuery = true)
    List<Object[]> findPendingTransactions(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT w FROM WalletHistory w WHERE w.userId = :userId AND w.transactionStatus IN :statuses AND w.purchasedLeafs > 0")
    Page<WalletHistory> findAllWithPurchasedLeafsGreaterThanZeroAndStatusAndUserId(
            @Param("userId") String userId,
            @Param("statuses") List<String> statuses,
            Pageable pageable
    );

}