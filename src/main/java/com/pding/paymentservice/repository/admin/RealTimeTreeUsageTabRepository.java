package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface RealTimeTreeUsageTabRepository extends JpaRepository<VideoPurchase, String> {

    @Query(value = "(" +
            " SELECT COALESCE(u.email, ''), COALESCE(vp.last_update_date, '') AS last_update_date,  COALESCE(vp.trees_consumed, 0), " +
            " 'VIDEO', COALESCE(pd.nickname, ''), COALESCE(pd.id, '') " +
            "            FROM video_purchase vp " +
            "LEFT JOIN users u ON vp.user_id = u.id " +
            "            INNER JOIN " +
            "            users pd ON vp.video_owner_user_id = pd.id " +
            "            WHERE (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
            "            AND (:endDate IS NULL OR  vp.last_update_date <= :endDate) " +
            "            AND (:searchString IS NULL OR u.email LIKE %:searchString% OR pd.nickname LIKE %:searchString%) " +
            "            ) " +
            "UNION ALL " +
            "(" +
            " SELECT COALESCE(u.email, ''), COALESCE(d.last_update_date, '')  AS last_update_date,  COALESCE(d.donated_trees, 0), " +
            " 'DONATION', COALESCE(pd.nickname, ''), COALESCE(pd.id, '') " +
            "            FROM donation d " +
            "            LEFT JOIN users u ON d.donor_user_id = u.id " +
            "            INNER JOIN users pd ON d.pd_user_id = pd.id " +
            "             AND (:startDate IS NULL OR d.last_update_date >= :startDate) " +
            "             AND (:endDate IS NULL OR  d.last_update_date <= :endDate) " +
            "             AND (:searchString IS NULL OR u.email LIKE %:searchString% OR pd.nickname LIKE %:searchString%) " +
            ")",
            countQuery = "SELECT COUNT(*) FROM \n" +
                    "( \n" +
                    "    (SELECT * FROM (\n" +
                    "        SELECT COALESCE(u.email, '') AS email, \n" +
                    "               COALESCE(vp.last_update_date, '') AS last_update_date,  \n" +
                    "               COALESCE(vp.trees_consumed, 0) AS trees_consumed, \n" +
                    "               'VIDEO' AS transaction_type, \n" +
                    "               COALESCE(pd.nickname, '') AS nickname, \n" +
                    "               COALESCE(pd.id, '') AS user_id \n" +
                    "        FROM video_purchase vp \n" +
                    "        LEFT JOIN users u ON vp.user_id = u.id \n" +
                    "        INNER JOIN users pd ON vp.video_owner_user_id = pd.id \n" +
                    "        WHERE  (:startDate IS NULL OR vp.last_update_date >= :startDate) \n" +
                    "         AND (:endDate IS NULL OR vp.last_update_date <= :endDate) \n" +
                    "         AND (:searchString IS NULL OR u.email LIKE %:searchString% OR pd.nickname LIKE %:searchString%) \n" +
                    "    ) AS t )\n" +
                    "    \n" +
                    "    UNION ALL \n" +
                    "    \n" +
                    "    (\n" +
                    "    SELECT * FROM ( \n" +
                    "        SELECT COALESCE(u.email, '') AS email, \n" +
                    "               COALESCE(d.last_update_date, '') AS last_update_date,  \n" +
                    "               COALESCE(d.donated_trees, 0) AS trees_consumed, \n" +
                    "               'DONATION' AS transaction_type, \n" +
                    "               COALESCE(pd.nickname, '') AS nickname, \n" +
                    "               COALESCE(pd.id, '') AS user_id \n" +
                    "        FROM donation d \n" +
                    "        LEFT JOIN users u ON d.donor_user_id = u.id \n" +
                    "        INNER JOIN users pd ON d.pd_user_id = pd.id \n" +
                    "        WHERE (:startDate IS NULL OR d.last_update_date >= :startDate) \n" +
                    "         AND (:endDate IS NULL OR d.last_update_date <= :endDate) \n" +
                    "         AND (:searchString IS NULL OR u.email LIKE %:searchString% OR pd.nickname LIKE %:searchString%) \n" +
                    "    ) AS t2 )\n" +
                    ") AS total_count",
            nativeQuery = true)
    Page<Object[]> getRealTimeTreeUsage(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("searchString") String searchString, Pageable pageable);

    @Query(value = "SELECT  COALESCE(SUM(donated_trees), 0) " +
            "FROM donation d WHERE d.pd_user_id = :userId " +
            "AND (:startDate IS NULL OR d.last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR  d.last_update_date <= :endDate) ",
            nativeQuery = true)
    BigDecimal getTotalTreesDonated(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT  COALESCE(SUM(w.trees),0) " +
            "FROM withdrawals w WHERE w.pd_user_id = :userId AND w.status IN ('COMPLETE', 'PENDING') " +
            "AND (:startDate IS NULL OR w.created_date >= :startDate) " +
            "AND (:endDate IS NULL OR  w.created_date <= :endDate) ",
            nativeQuery = true)
    BigDecimal getTotalTreesTransactedForVideos(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
