package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferredPdDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OtherServicesTablesNativeQueryRepository extends JpaRepository<VideoPurchase, String> {
    @Query(value = "SELECT id, COALESCE(email, '') AS email, COALESCE(pd_type, '') AS pdType, COALESCE(nickname, '') AS nickname, COALESCE(linked_stripe_id, '') AS linkedStripeId FROM users WHERE id = :userId", nativeQuery = true)
    List<Object[]> findUserInfoByUserId(@Param("userId") String userId);

    @Query(value = "SELECT * FROM referrals WHERE referred_pd_user_id = :referredPdUserId", nativeQuery = true)
    List<Object[]> findReferralDetailsByReferredPdUserId(@Param("referredPdUserId") String referredPdUserId);

    @Query(value = "SELECT COALESCE(u.nickname, '') as nickname, " +
            "COALESCE(u.pd_type, '') as pd_type, " +
            "COALESCE(u.created_date, '') as created_date, " +
            "COALESCE(e.trees_earned, '') as trees_earned, " +
            "COALESCE(e.leafs_earned, '') as leafs_earned, " +
            "COALESCE(r.referred_pd_user_id, '') as referred_pd_user_id, " +
            "COALESCE((SELECT w.created_date FROM withdrawals w " +
            "WHERE w.pd_user_id = u.id ORDER BY w.created_date DESC LIMIT 1), '') as last_exchange_date " +
            "FROM referrals r " +
            "JOIN users u ON r.referred_pd_user_id = u.id " +
            "JOIN earning e ON e.user_id = r.referred_pd_user_id " +
            "WHERE r.referrer_pd_user_id = :referrerPdUserId",
            countQuery = "SELECT count(*) " +
                    "FROM referrals r " +
                    "JOIN users u ON r.referred_pd_user_id = u.id " +
                    "JOIN earning e ON e.user_id = r.referred_pd_user_id " +
                    "WHERE r.referrer_pd_user_id = :referrerPdUserId",
            nativeQuery = true)
    Page<Object[]> getDetailsOfAllTheReferredPd(String referrerPdUserId, Pageable pageable);

    @Query(value = "SELECT rc.id as referralCommissionId, rc.withdrawal_id as withdrawalId, rc.referrer_pd_user_id as referrerPdUserId, " +
            "rc.commission_percent as commissionPercent, rc.commission_amount_in_trees as commissionAmountInTrees, " +
            "rc.commission_amount_in_cents as commissionAmountInCents, rc.created_date as referralCommissionCreatedDate, " +
            "rc.updated_date as referralCommissionUpdatedDate, rc.commission_transfer_status as commissionTransferStatus, " +
            "w.pd_user_id as withdrawalUserId, w.trees as withdrawalTrees, w.leafs withdrawalLeafs, w.status as withdrawalStatus, " +
            "w.created_date as withdrawalCreatedDate, w.updated_date as withdrawalUpdatedDate, " +
            "u.nickname as userNickname, u.pd_type as pdType " +
            "FROM referral_commission rc " +
            "INNER JOIN withdrawals w ON rc.withdrawal_id = w.id " +
            "INNER JOIN users u ON w.pd_user_id = u.id " +
            "WHERE rc.referrer_pd_user_id = :referrerPdUserId " +
            "AND (:startDate IS NULL OR rc.created_date >= :startDate) " +
            "AND (:endDate IS NULL OR rc.created_date <= :endDate) " +
            "AND (COALESCE(:searchString, '') = '' OR u.nickname LIKE CONCAT('%', :searchString, '%'))",
            countQuery = "SELECT COUNT(*) FROM referral_commission rc " +
                    "INNER JOIN withdrawals w ON rc.withdrawal_id = w.id " +
                    "INNER JOIN users u ON w.pd_user_id = u.id " +
                    "WHERE rc.referrer_pd_user_id = :referrerPdUserId " +
                    "AND (:startDate IS NULL OR rc.created_date >= :startDate) " +
                    "AND (:endDate IS NULL OR rc.created_date <= :endDate) " +
                    "AND (COALESCE(:searchString, '') = '' OR u.nickname LIKE CONCAT('%', :searchString, '%'))",
            nativeQuery = true)
    Page<Object[]> getReferralCommissionDetailsWithFilters(
            @Param("referrerPdUserId") String referrerPdUserId,
            Pageable pageable,
            @Param("searchString") String searchString,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query(value = "SELECT rc.id AS referralCommissionId, " +
            "rc.withdrawal_id AS withdrawalId, " +
            "rc.referrer_pd_user_id AS referrerPdUserId, " +
            "rc.commission_percent AS referrerCommissionPercent, " +
            "rc.commission_amount_in_trees AS referrerCommissionAmountInTrees, " +
            "rc.created_date AS referrerCommissionCreatedDate, " +
            "rc.updated_date AS referrerCommissionUpdatedDate, " +
            "rc.commission_transfer_status AS referrerCommissionTransferStatus, " +
            "COALESCE(u.nickname, '') AS referrerUserNickname, " +
            "COALESCE(u.email, '') AS referrerUserEmail, " +
            "COALESCE(u.pd_type, '') AS referrerPdType, " +
            "COALESCE(u.linked_stripe_id, '') AS linkedStripeId, " +
            "w.pd_user_id AS referredPdUserId " +
            "FROM referral_commission rc " +
            "INNER JOIN users u ON rc.referrer_pd_user_id = u.id " +
            "INNER JOIN withdrawals w ON rc.withdrawal_id = w.id " +
            "WHERE (:searchString IS NULL " +
            "       OR u.email LIKE %:searchString% " +
            "       OR u.nickname LIKE %:searchString%) " +
            "ORDER BY " +
            "CASE WHEN rc.commission_transfer_status = 'TRANSFER_PENDING' THEN 0 ELSE 1 END, " +
            "rc.updated_date ASC",
            countQuery = "SELECT COUNT(*) FROM referral_commission rc " +
                    "INNER JOIN users u ON rc.referrer_pd_user_id = u.id " +
                    "INNER JOIN withdrawals w ON rc.withdrawal_id = w.id " +
                    "WHERE (:searchString IS NULL " +
                    "       OR u.email LIKE %:searchString% " +
                    "       OR u.nickname LIKE %:searchString%)",
            nativeQuery = true)
    Page<Object[]> getReferralCommissionHistoryForAdminDashboard(
            Pageable pageable,
            @Param("searchString") String searchString
    );

    @Query(value = "SELECT COUNT(*) FROM referrals WHERE referrer_pd_user_id = :referrerPdUserId", nativeQuery = true)
    Integer totalNumberOfReferredPdByCurrentPd(@Param("referrerPdUserId") String referrerPdUserId);

    @Query(value = "SELECT COUNT(DISTINCT referred_pd_user_id) FROM referrals", nativeQuery = true)
    Integer totalNumberOfReferredPdInSystem();

    @Query(value = "SELECT COALESCE(u.id, '') AS referredPdUserId, " +
            "COALESCE(u.email, '') AS referredPdUserEmail, " +
            "COALESCE(u.nickname, '') AS referredPdUserNickname, " +
            "COALESCE(e.trees_earned, 0) AS treesEarned, " +
            "COALESCE(w.total_trees_withdrawn, 0) AS totalTreesWithdrawn " +
            "FROM referrals r " +
            "JOIN users u ON r.referred_pd_user_id = u.id " +
            "LEFT JOIN (SELECT user_id, SUM(trees_earned) AS trees_earned FROM earning GROUP BY user_id) e ON e.user_id = u.id " +
            "LEFT JOIN (SELECT pd_user_id, SUM(trees) AS total_trees_withdrawn FROM withdrawals GROUP BY pd_user_id) w ON w.pd_user_id = u.id " +
            "WHERE r.referrer_pd_user_id = :referrerPdUserId",
            countQuery = "SELECT COUNT(*) " +
                    "FROM referrals r " +
                    "JOIN users u ON r.referred_pd_user_id = u.id " +
                    "LEFT JOIN (SELECT user_id, SUM(trees_earned) AS trees_earned FROM earning GROUP BY user_id) e ON e.user_id = u.id " +
                    "LEFT JOIN (SELECT pd_user_id, SUM(trees) AS total_trees_withdrawn FROM withdrawals GROUP BY pd_user_id) w ON w.pd_user_id = u.id " +
                    "WHERE r.referrer_pd_user_id = :referrerPdUserId",
            nativeQuery = true)
    Page<Object[]> getReferredPdDetails(@Param("referrerPdUserId") String referrerPdUserId, Pageable pageable);
}
