package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.PhotoPurchase;
import com.pding.paymentservice.models.tables.inner.PhotoEarningsAndSales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Query("SELECT pp.postId, COALESCE(SUM(pp.treesConsumed), 0), COUNT(pp) FROM PhotoPurchase pp WHERE pp.postId IN :postIds and pp.isRefunded = false GROUP BY pp.postId")
    List<Object[]> getTotalTreesEarnedAndSalesCountForPostIds(@Param("postIds") List<String> postIds);

    default Map<String, PhotoEarningsAndSales> getTotalTreesEarnedAndSalesCountMapForPostIds(List<String> postIds) {
        List<Object[]> results = getTotalTreesEarnedAndSalesCountForPostIds(postIds);

        // Create a map with default values for cases where a record is not found
        Map<String, PhotoEarningsAndSales> resultMap = new HashMap<>();
        for (String postId : postIds) {
            resultMap.put(postId, new PhotoEarningsAndSales(BigDecimal.ZERO, 0L));
        }

        // Update values for records that are found
        results.forEach(result -> {
            String postId = (String) result[0];
            BigDecimal totalTreesEarned = (BigDecimal) result[1];
            Long totalSalesCount = (Long) result[2];
            resultMap.put(postId, new PhotoEarningsAndSales(totalTreesEarned, totalSalesCount));
        });

        return resultMap;
    }
}
