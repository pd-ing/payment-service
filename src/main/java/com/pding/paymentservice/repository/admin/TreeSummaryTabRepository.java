package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.payload.response.TreeSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TreeSummaryTabRepository extends JpaRepository<VideoPurchase, String> {

//    @Query(value = "SELECT COALESCE(u.id, ''), COALESCE(u.nickname, ''), COALESCE(u.email, ''), COALESCE(u.pd_type, ''), " +
//            "COALESCE(SUM(vp.trees_consumed), 0) " +
//            "FROM users u " +
//            "INNER JOIN video_purchase vp ON vp.video_owner_user_id = u.id " +
//            "AND (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
//            "AND (:endDate IS NULL OR  vp.last_update_date <= :endDate) " +
//            "AND (:searchString IS NULL OR u.email LIKE %:searchString%) " +
//            "GROUP BY u.id, u.nickname, u.email, u.pd_type",
//            countQuery = "SELECT COUNT(*) FROM users u WHERE (:searchString IS NULL OR u.email LIKE %:searchString%)",
//            nativeQuery = true)
//    Page<Object[]> getTreesRevenueForPd(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("searchString") String searchString, Pageable pageable);


    @Query(value = "SELECT COALESCE(u.id, ''), COALESCE(u.nickname, ''), COALESCE(u.email, ''), COALESCE(u.pd_type, ''), " +
            "COALESCE(SUM(vp.trees_consumed), 0) AS totalTreeRevenue " +
            "FROM users u " +
            "INNER JOIN video_purchase vp ON vp.video_owner_user_id = u.id " +
            "AND (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR  vp.last_update_date <= :endDate) " +
            "AND (:searchString IS NULL OR u.email LIKE %:searchString% OR u.nickname LIKE %:searchString%) " +
            "GROUP BY u.id, u.nickname, u.email, u.pd_type " +
            "ORDER BY totalTreeRevenue DESC ",
            countQuery = "SELECT COUNT(*) FROM users u WHERE (:searchString IS NULL OR u.email LIKE %:searchString%)",
            nativeQuery = true)
    Page<Object[]> getTreesRevenueForPd(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("searchString") String searchString, Pageable pageable);

    @Query(value = "SELECT  COALESCE(SUM(donated_trees), 0) " +
            "FROM donation d WHERE d.pd_user_id = :userId " +
            "AND (:startDate IS NULL OR d.last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR  d.last_update_date <= :endDate) ",
            nativeQuery = true)
    BigDecimal getTreesDonatedForPd(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT  COALESCE(SUM(w.trees),0) " +
            "FROM withdrawals w WHERE w.pd_user_id = :userId AND w.status IN ('COMPLETE', 'PENDING') " +
            "AND (:startDate IS NULL OR w.created_date >= :startDate) " +
            "AND (:endDate IS NULL OR  w.created_date <= :endDate) ",
            nativeQuery = true)
    BigDecimal getTotalExchangedTreesForPd(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT  COALESCE(SUM(ew.trees_earned),0) " +
            "FROM earning ew WHERE ew.user_id = :userId " +
            "AND (:startDate IS NULL OR ew.created_date >= :startDate) " +
            "AND (:endDate IS NULL OR  ew.created_date <= :endDate) ",
            nativeQuery = true)
    BigDecimal getTotalUnExchangedTreesForPd(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

//    @Query(value = "SELECT (COALESCE(SUM(vp.trees_consumed), 0) + COALESCE(SUM(d.donated_trees), 0)) AS totalTreeRevenue " +
//            "FROM video_purchase vp, donation d",
//            nativeQuery = true)
//    BigDecimal getTotalTreeRevenueForAllUsers();

    @Query(value = "SELECT COALESCE(SUM(vp.trees_consumed), 0) FROM video_purchase vp", nativeQuery = true)
    BigDecimal getTotalTreesConsumedForVideos();

    @Query(value = "SELECT COALESCE(SUM(d.donated_trees), 0) FROM donation d", nativeQuery = true)
    BigDecimal getTotalDonatedTrees();

    @Query(value = "SELECT COALESCE(SUM(w.trees), 0) AS totalExchangedTrees FROM withdrawals w WHERE w.status IN ('COMPLETE', 'PENDING')", nativeQuery = true)
    BigDecimal getTotalExchangedTreesForAllUsers();

    @Query(value = "SELECT COALESCE(SUM(ew.trees_earned), 0) AS unExchangedTrees FROM earning ew", nativeQuery = true)
    BigDecimal getUnExchangedTreesForAllUsers();

}

