package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.InChatMediaTrading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaTradingRepository extends JpaRepository<InChatMediaTrading, String> {
    boolean existsByMessageId(String messageId);
    Optional<InChatMediaTrading> findByMessageId(String messageId);

}
