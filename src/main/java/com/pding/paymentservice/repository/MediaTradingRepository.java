package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.InChatMediaTrading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MediaTradingRepository extends JpaRepository<InChatMediaTrading, String> {
    boolean existsByMessageId(String messageId);
    Optional<InChatMediaTrading> findByMessageId(String messageId);

    @Query(value = "Select mt from InChatMediaTrading mt where mt.userId = :userId and mt.pdId = :pdId and mt.transactionStatus = 'PAID'")
    Slice<InChatMediaTrading> findByUserIdAndPdId(String userId, String pdId, Pageable pageable);

}
