package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query(value = "SELECT COALESCE(SUM(vp.trees_consumed), 0) AS totalTreesVideos, " +
            "COALESCE(SUM(d.donated_trees), 0) AS totalTreesDonations " +
            "FROM video_purchase vp " +
            "LEFT JOIN donation d ON vp.user_id = d.donor_user_id " +
            "WHERE vp.user_id = :userId", nativeQuery = true)
    Object[] getTotalTreesSpentOnVideoAndDonationByUserId(String userId);


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
}
