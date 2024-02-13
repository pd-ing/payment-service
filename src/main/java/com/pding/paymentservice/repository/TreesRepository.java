package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
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
}