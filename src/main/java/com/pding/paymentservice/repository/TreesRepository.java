package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface TreesRepository extends JpaRepository<VideoPurchase, String> {
    @Query(value = "SELECT user_id, SUM(totalTreesSpent) AS totalTreesSpent " +
            "FROM (" +
            "   SELECT COALESCE(vp.user_id, d.donor_user_id) AS user_id, " +
            "          COALESCE(SUM(vp.trees_consumed), 0) + COALESCE(SUM(d.donated_trees), 0) AS totalTreesSpent " +
            "   FROM video_purchase vp " +
            "   LEFT JOIN donation d ON vp.user_id = d.donor_user_id " +
            "   GROUP BY user_id " +
            "   UNION ALL " +
            "   SELECT COALESCE(vp.user_id, d.donor_user_id) AS user_id, " +
            "          COALESCE(SUM(vp.trees_consumed) + SUM(d.donated_trees), 0) AS totalTreesSpent " +
            "   FROM video_purchase vp " +
            "   RIGHT JOIN donation d ON vp.user_id = d.donor_user_id " +
            "   GROUP BY user_id" +
            ") AS subquery " +
            "GROUP BY user_id " +
            "ORDER BY totalTreesSpent DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> getUserTotalTreesSpentWithLimit(@Param("limit") Long limit);

    @Query(value = "(SELECT " +
            "  vp.last_update_date as last_update_date, " +
            "  'video purchase' as type, " +
            "  COALESCE(u.profile_id, '') as pd_profile_id, " +
            "  vp.trees_consumed as amount " +
            "FROM " +
            "  video_purchase vp " +
            "  LEFT JOIN users u ON vp.video_owner_user_id = u.id " +
            "WHERE " +
            "  vp.user_id = :userId) " +
            "UNION ALL " +
            "(SELECT " +
            "  d.last_update_date as last_update_date, " +
            "  'donation' as type, " +
            "  COALESCE(u.profile_id, '') as pd_profile_id, " +
            "  d.donated_trees as amount " +
            "FROM " +
            "  donation d " +
            "  LEFT JOIN users u ON d.donor_user_id = u.id " +
            "WHERE " +
            "  d.donor_user_id = :userId) " +
            "ORDER BY last_update_date DESC",
            countQuery = "SELECT COUNT(*) FROM (SELECT vp.last_update_date FROM video_purchase vp WHERE vp.user_id = :userId " +
                    "UNION ALL SELECT d.last_update_date FROM donation d WHERE d.donor_user_id = :userId) AS total",
            nativeQuery = true)
    Page<Object[]> getTreesSpentHistory(String userId, Pageable pageable);
}