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

    @Query(value = "SELECT COALESCE(u.nickname, ''), COALESCE(u.email, ''), COALESCE(u.pd_type, ''), " +
            "(COALESCE(SUM(vp.trees_consumed), 0) + COALESCE(SUM(d.donated_trees), 0)), " +
            "COALESCE(SUM(w.trees), 0), " +
            "COALESCE(SUM(ew.trees_earned), 0) " +
            "FROM users u " +
            "LEFT JOIN video_purchase vp ON vp.user_id = u.id " +
            "LEFT JOIN donation d ON d.pd_user_id = u.id " +
            "LEFT JOIN withdrawals w ON w.pd_user_id = u.id " +
            "LEFT JOIN  earning ew ON ew.user_id = u.id " +
            "WHERE w.status = 'COMPLETE' " +
            "AND (:startDate IS NULL OR COALESCE(LEAST(vp.last_update_date, d.last_update_date, w.created_date, ew.created_date), :startDate) >= :startDate) " +
            "AND (:endDate IS NULL OR  COALESCE(GREATEST(vp.last_update_date, d.last_update_date, w.created_date, ew.created_date), :endDate) <= :endDate) " +
            "AND (:searchString IS NULL OR u.email LIKE %:searchString%) " +
            "GROUP BY u.nickname, u.email, u.pd_type",
            countQuery = "SELECT COUNT(*) FROM users u wh WHERE (:searchString IS NULL OR u.email LIKE %?3%) ",
            nativeQuery = true)
    Page<Object[]> getTreesSummaryByUsers(LocalDate startDate, LocalDate endDate, String searchString, Pageable pageable);

    @Query(value = "SELECT (COALESCE(SUM(vp.trees_consumed), 0) + COALESCE(SUM(d.donated_trees), 0)) AS totalTreeRevenue " +
            "FROM video_purchase vp, donation d" ,
            nativeQuery = true)
    BigDecimal getTotalTreeRevenueForAllUsers();

    @Query(value ="SELECT COALESCE(SUM(w.trees), 0) AS totalExchangedTrees FROM withdrawals w WHERE w.status = 'COMPLETE'", nativeQuery = true)
    BigDecimal getTotalExchangedTreesForAllUsers();

    @Query(value ="SELECT COALESCE(SUM(ew.trees_earned), 0) AS unExchangedTrees FROM earning ew", nativeQuery = true)
    BigDecimal getUnExchangedTreesForAllUsers();

}

