package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface StatusTabRepository extends JpaRepository<WalletHistory, String> {
    //StatusTab : Field -> totalTreesCharged
    @Query(value = "SELECT COALESCE(SUM(w.purchased_trees), 0) " +
            "FROM wallet_history w " +
            "WHERE w.user_id = :userId " +
            "AND (w.transaction_status = 'success' OR w.transaction_status = 'paymentCompleted')", nativeQuery = true)
    BigDecimal getTotalTreesChargedByUserId(String userId);

    //StatusTab : Field -> currentHoldingTrees
    @Query(value = "SELECT COALESCE(SUM(w.trees), 0) " +
            "FROM wallet w " +
            "WHERE w.user_id = :userId", nativeQuery = true)
    BigDecimal getCurrentHoldingTreesByUserId(String userId);

    //StatusTab : Field -> object[0] -> totalTreesSpendInVideoPurchase
    //            Field -> object[1] -> totalTreesDonated
    //            Field -> object[0]+object[1] = totalTreesSpent
    @Query(value = "SELECT "
            + "(SELECT COALESCE(SUM(trees_consumed), 0) "
            + " FROM video_purchase "
            + " WHERE user_id = :userId) AS totalTreesVideos, "
            + "(SELECT COALESCE(SUM(donated_trees), 0) "
            + " FROM donation "
            + " WHERE donor_user_id = :userId) AS totalDonatedTrees", nativeQuery = true)
    Object[] getTotalTreesSpentOnVideoAndDonationByUserId(@Param("userId") String userId);


    //StatusTab : Field -> Object[0] treesAddedInCurrentMonth And
    //                     Object[1] treesAddedInPreviousMonth, Use this to calculate the MOM%
    @Query(value = "SELECT " +
            "    COALESCE(SUM(CASE WHEN MONTH(w.purchase_date) = MONTH(CURRENT_DATE()) " +
            "                      AND YEAR(w.purchase_date) = YEAR(CURRENT_DATE()) " +
            "                 THEN w.purchased_trees ELSE 0 END), 0) AS treesPurchasedThisMonth, " +
            "    COALESCE(SUM(CASE WHEN (MONTH(w.purchase_date) = 12 AND YEAR(w.purchase_date) = YEAR(CURRENT_DATE()) - 1) " +
            "                          OR (MONTH(w.purchase_date) = MONTH(CURRENT_DATE()) - 1 AND YEAR(w.purchase_date) = YEAR(CURRENT_DATE())) " +
            "                 THEN w.purchased_trees ELSE 0 END), 0) AS treesPurchasedLastMonth " +
            "FROM wallet_history w " +
            "WHERE w.user_id = :userId " +
            "  AND (w.transaction_status = 'success' OR w.transaction_status = 'paymentCompleted')", nativeQuery = true)
    Object[] getMonthlyTreesPurchased(String userId);


    //StatusTab : Field -> totalVideosPurchased
    @Query(value = "SELECT COUNT(DISTINCT video_id) " +
            "FROM video_purchase " +
            "WHERE user_id = :userId", nativeQuery = true)
    BigDecimal getTotalVideosPurchasedByUserId(String userId);

    @Query(value = "SELECT COALESCE(SUM(w.trees), 0) " +
            "FROM withdrawals w WHERE w.pd_user_id = :pdId AND w.status IN ('COMPLETE', 'PENDING')  ", nativeQuery = true)
    BigDecimal getTotalTreesExchangedByPd(String pdId);

    @Query(value = "SELECT COALESCE(SUM(ew.trees_earned), 0) " +
            "FROM earning ew " +
            "WHERE ew.user_id = :pdId ", nativeQuery = true)
    BigDecimal getTotalHoldingTreesByPd(String pdId);

    @Query(value = "SELECT SUM(trees) AS treesPurchasedThisMonth \n" +
            "FROM ( \n" +
            "\tSELECT COALESCE(SUM(vp.trees_consumed), 0) AS trees \n" +
            "\tFROM video_purchase vp  \n" +
            "    WHERE vp.video_owner_user_id = :pdId \n" +
            "    AND MONTH(vp.last_update_date) = MONTH(CURRENT_DATE()) \n" +
            "    AND YEAR(vp.last_update_date) = YEAR(CURRENT_DATE()) \n" +
            "UNION ALL    \n" +
            "\tSELECT COALESCE(SUM(d.donated_trees), 0) AS trees \n" +
            "\tFROM  donation d \n" +
            "\tWHERE d.pd_user_id = :pdId \n" +
            "\tAND MONTH(d.last_update_date) = MONTH(CURRENT_DATE()) \n" +
            "\tAND YEAR(d.last_update_date) = YEAR(CURRENT_DATE()) \n" +
            ") AS temp", nativeQuery = true)
    BigDecimal getCurrentMonthTreeRevenueForPd(String pdId);

    @Query(value = "SELECT SUM(trees) AS treesEarnedPrevMonth \n" +
            "FROM ( \n" +
            "\tSELECT COALESCE(SUM(vp.trees_consumed), 0) AS trees \n" +
            "\tFROM video_purchase vp  \n" +
            "    WHERE vp.video_owner_user_id = :pdId \n" +
            "    AND \n" +
            "    (( MONTH(vp.last_update_date) = 12 AND YEAR(vp.last_update_date) = YEAR(CURRENT_DATE()) - 1) \n" +
            "    OR ( MONTH(vp.last_update_date) = MONTH(CURRENT_DATE()) - 1  AND YEAR(vp.last_update_date) = YEAR(CURRENT_DATE()) )) \n" +
            "UNION ALL    \n" +
            "\tSELECT COALESCE(SUM(d.donated_trees), 0) AS trees \n" +
            "\tFROM  donation d \n" +
            "\tWHERE d.pd_user_id = :pdId \n" +
            "\tAND \n" +
            "    (( MONTH(d.last_update_date) = 12 AND YEAR(d.last_update_date) = YEAR(CURRENT_DATE()) - 1) \n" +
            "    OR ( MONTH(d.last_update_date) = MONTH(CURRENT_DATE()) - 1  AND YEAR(d.last_update_date) = YEAR(CURRENT_DATE()) )) \n" +
            ") AS temp2", nativeQuery = true)
    BigDecimal getPreviousMonthTreeRevenueForPd(String pdId);

    @Query(value = "SELECT COALESCE(SUM(trees_consumed), 0) " +
            "FROM video_purchase " +
            "WHERE video_owner_user_id = :pdId", nativeQuery = true)
    BigDecimal getVideoSalesTreeForPd(String pdId);

    @Query(value = "SELECT COALESCE(SUM(donated_trees), 0) " +
            "FROM donation " +
            "WHERE donor_user_id = :pdId", nativeQuery = true)
    BigDecimal getGiftGivingTreeForPd(String pdId);

}
