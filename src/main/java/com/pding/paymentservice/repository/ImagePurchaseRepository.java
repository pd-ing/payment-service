package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.ImagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagePurchaseRepository extends JpaRepository<ImagePurchase, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);

    List<ImagePurchase> findByUserIdAndPostIdIn(String userId, List<String> postIds);
}
