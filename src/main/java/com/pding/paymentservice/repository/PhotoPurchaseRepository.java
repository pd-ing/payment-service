package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.PhotoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PhotoPurchase entity
 */
@Repository
public interface PhotoPurchaseRepository extends JpaRepository<PhotoPurchase, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);

    List<PhotoPurchase> findByUserIdAndPostIdIn(String userId, List<String> postIds);

    List<PhotoPurchase> findByUserIdAndPostId(String userId, String postId);

    Slice<PhotoPurchase> findByUserIdAndPostOwnerUserId(String userId, String pdId, Pageable pageable);

    @Query(value = "select distinct post_owner_user_id from photo_purchase where user_id = :userId", nativeQuery = true)
    Page<String> getAllPdUserIdWhosePostsArePurchasedByUser(String userId, Pageable pageable);

    Page<PhotoPurchase> findAllByPostIdOrderByLastUpdateDateDesc(String postId, Pageable pageable);
}
