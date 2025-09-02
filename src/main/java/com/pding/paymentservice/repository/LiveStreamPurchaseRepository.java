package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.LiveStreamPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveStreamPurchaseRepository extends JpaRepository<LiveStreamPurchase, String> {
    List<LiveStreamPurchase> findByBuyerUserIdAndLivestreamId(String buyerUserId, String livestreamId);
}