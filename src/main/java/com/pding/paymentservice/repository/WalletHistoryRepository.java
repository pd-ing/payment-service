package com.pding.paymentservice.repository;


import com.pding.paymentservice.models.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

}