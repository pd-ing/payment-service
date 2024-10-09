package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.ImagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagePurchaseRepository extends JpaRepository<ImagePurchase, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);

    List<ImagePurchase> findByUserIdAndPostIdIn(String userId, List<String> postIds);

    @Query(value = "SELECT user_id, leaf_amount FROM image_post WHERE post_id = :postId", nativeQuery = true)
    List<Object[]> fetchOwnerAndPriceByPostId(@Param("postId") String postId);
}
