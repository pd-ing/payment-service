package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

public interface TreesRepository extends JpaRepository<VideoPurchase, String> {
    @Query(value = "SELECT user_id, 2.5 * SUM(totalSpent) AS totalTreesLeafsSpent \n" +
            "FROM ( \n" +
            "    SELECT user_id, SUM(totalTreesSpent) AS totalSpent \n" +
            "    FROM ( \n" +
            "        SELECT COALESCE(vp.user_id, d.donor_user_id) AS user_id, \n" +
            "               COALESCE(SUM(vp.trees_consumed), 0) + COALESCE(SUM(d.donated_trees), 0) AS totalTreesSpent \n" +
            "        FROM video_purchase vp \n" +
            "        LEFT JOIN donation d ON vp.user_id = d.donor_user_id \n" +
            "        GROUP BY COALESCE(vp.user_id, d.donor_user_id) \n" +
            "        UNION ALL \n" +
            "        SELECT COALESCE(vp.user_id, d.donor_user_id) AS user_id, \n" +
            "               COALESCE(SUM(vp.trees_consumed), 0) + COALESCE(SUM(d.donated_trees), 0) AS totalTreesSpent \n" +
            "        FROM video_purchase vp \n" +
            "        RIGHT JOIN donation d ON vp.user_id = d.donor_user_id \n" +
            "        GROUP BY COALESCE(vp.user_id, d.donor_user_id) \n" +
            "    ) AS subquery1 \n" +
            "    GROUP BY user_id \n" +
            "    UNION ALL \n" +
            "    SELECT user_id, SUM(totalLeaves) AS totalSpent \n" +
            "    FROM ( \n" +
            "        SELECT user_id, COALESCE(SUM(leafs_transacted), 0) AS totalLeaves \n" +
            "        FROM call_purchase \n" +
            "        GROUP BY user_id \n" +
            "        UNION ALL \n" +
            "        SELECT user_id, COALESCE(SUM(leafs_transacted), 0) AS totalLeaves \n" +
            "        FROM message_purchase \n" +
            "        GROUP BY user_id \n" +
            "        UNION ALL \n" +
            "        SELECT donor_user_id AS user_id, COALESCE(SUM(donated_leafs), 0) AS totalLeaves \n" +
            "        FROM donation \n" +
            "        GROUP BY donor_user_id \n" +
            "    ) AS subquery2 \n" +
            "    GROUP BY user_id \n" +
            ") AS combined \n" +
            " where user_id not in (:blockedUsers) \n" +
            " GROUP BY user_id \n" +
            " ORDER BY totalTreesLeafsSpent DESC " +
            " LIMIT :limit", nativeQuery = true)
    List<Object[]> getUserTotalTreesSpentWithLimit(@Param("limit") Long limit, @Param("blockedUsers") List<String> blockedUsers);

    @Query(value =
                " (SELECT vp.last_update_date        as last_update_date," +
                "         'video_purchase'           as type," +
                "         COALESCE(u.profile_id, '') as pd_profile_id," +
                "         vp.trees_consumed          as amount" +
                "  FROM video_purchase vp" +
                "           LEFT JOIN users u ON vp.video_owner_user_id = u.id" +
                "  WHERE vp.user_id = :userId)" +
                " UNION ALL" +
                " (SELECT d.last_update_date         as last_update_date," +
                "         'donation'                 as type," +
                "         COALESCE(u.profile_id, '') as pd_profile_id," +
                "         d.donated_trees            as amount" +
                "  FROM donation d" +
                "           LEFT JOIN users u ON d.donor_user_id = u.id" +
                "  WHERE d.donor_user_id = :userId)" +
                " union all" +
                " (select etp.purchased_date as last_update_date," +
                "         'ticket'           as type," +
                "         '-'                as pd_profile_id," +
                "         etp.trees_consumed as amount" +
                "  from exposure_ticket_purchase etp" +
                "           LEFT JOIN users u ON etp.user_id = u.id" +
                "  WHERE etp.is_give_by_admin is not true and etp.user_id = :userId)" +
                " ORDER BY last_update_date DESC",
            countQuery =
                " select count(*) " +
                " from ((SELECT vp.last_update_date as last_update_date " +
                "        FROM video_purchase vp " +
                "                 LEFT JOIN users u ON vp.video_owner_user_id = u.id " +
                "        WHERE vp.user_id = :userId) " +
                "       UNION ALL " +
                "       (SELECT d.last_update_date as last_update_date " +
                "        FROM donation d " +
                "                 LEFT JOIN users u ON d.donor_user_id = u.id " +
                "        WHERE d.donor_user_id = :userId) " +
                "       union all " +
                "       (select etp.purchased_date as last_update_date " +
                "        from exposure_ticket_purchase etp " +
                "                 LEFT JOIN users u ON etp.user_id = u.id " +
                "        WHERE etp.is_give_by_admin is not true and etp.user_id = :userId)) as total",
            nativeQuery = true)
    Page<Object[]> getTreesSpentHistory(String userId, Pageable pageable);


}
