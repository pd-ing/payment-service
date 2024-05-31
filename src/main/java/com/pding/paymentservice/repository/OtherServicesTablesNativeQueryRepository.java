package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.other.services.tables.dto.ReferralCommissionDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Query(value = "SELECT COALESCE(u.nickname, ' ') AS nickname, COALESCE(u.email, ' ') AS email, \n" +
            "COALESCE(ew.trees_earned, 0.00) AS trees_earned, \n" +
            "COALESCE(FROM_UNIXTIME(u.created_date), ' ') AS created_date, \n" +
            " ref_code.referral_code , COALESCE(u.pd_type, ' ') \n" +
            "FROM referrals r \n" +
            "INNER JOIN  users u ON u.id = r.referred_pd_user_id \n" +
            "INNER JOIN  earning ew ON ew.user_id = u.id  and ew.user_id = r.referred_pd_user_id \n" +
            "INNER JOIN  (SELECT COALESCE (referral_code, '') AS referral_code FROM users \n" +
            "   WHERE id = :pdUserId) AS ref_code ON 1 = 1\n" +
            "where r.referrer_pd_user_id COLLATE utf8mb4_unicode_ci = :pdUserId\n" +
            "AND (:startDate IS NULL OR FROM_UNIXTIME(u.created_date) >= :startDate) \n" +
            "            AND (:endDate IS NULL OR  FROM_UNIXTIME(u.created_date) <= :endDate) \n" +
            "             AND (:searchString IS NULL OR u.email LIKE %:searchString% OR u.nickname LIKE %:searchString%)",
            countQuery = "SELECT COUNT(*) \n" +
                    "FROM  referrals r \n" +
                    "INNER JOIN  users u ON u.id = r.referred_pd_user_id \n" +
                    "INNER JOIN  earning ew ON ew.user_id = u.id  and ew.user_id = r.referred_pd_user_id \n" +
                    "WHERE r.referrer_pd_user_id COLLATE utf8mb4_unicode_ci = :pdUserId \n" +
                    "AND (:startDate IS NULL OR FROM_UNIXTIME(u.created_date) >= :startDate) \n" +
                    "            AND (:endDate IS NULL OR  FROM_UNIXTIME(u.created_date) <= :endDate) \n" +
                    "             AND (:searchString IS NULL OR u.email LIKE %:searchString% OR u.nickname LIKE %:searchString%)",
            nativeQuery = true)
    Page<Object[]> getListOfAllTheReferredPds (String pdUserId, LocalDate startDate, LocalDate endDate, String searchString, Pageable pageable);


    @Query(value = "SELECT COALESCE(u.nickname, ' ') AS nickname, COALESCE(u.email, ' ') AS email, \n" +
            "COALESCE(FROM_UNIXTIME(u.created_date), ' ') AS created_date, \n" +
            "COALESCE(u.pd_type, ' ') ,\n" +
            "COALESCE(ew.trees_earned, 0.00) AS trees_earned, \n" +
            "COALESCE(w.created_date, ' ') AS withdrawal_date , \n" +
            "COALESCE(w.trees, 0.00) AS trees_exhanged \n" +
            "FROM referrals r \n" +
            "INNER JOIN  users u ON u.id = r.referred_pd_user_id \n" +
            "INNER JOIN  earning ew ON ew.user_id = u.id  and ew.user_id = r.referred_pd_user_id \n" +
            "INNER JOIN  withdrawals w ON w.pd_user_id = r.referred_pd_user_id AND w.pd_user_id  = u.id AND w.pd_user_id  = ew.user_id\n" +
            "WHERE r.referred_pd_user_id COLLATE utf8mb4_unicode_ci = :pdUserId " +
            "ORDER BY w.created_date DESC",
            countQuery = "SELECT COUNT(*) FROM  referrals r \n" +
                    "INNER JOIN  users u ON u.id = r.referred_pd_user_id \n" +
                    "INNER JOIN  earning ew ON ew.user_id = u.id  and ew.user_id = r.referred_pd_user_id \n" +
                    "INNER JOIN  withdrawals w ON w.pd_user_id = r.referred_pd_user_id AND w.pd_user_id  = u.id AND w.pd_user_id  = ew.user_id\n" +
                    "where r.referred_pd_user_id COLLATE utf8mb4_unicode_ci = :pdUserId",
            nativeQuery = true)
    Page<Object[]> getWithdrawalHistoryForReferredPds (String pdUserId, Pageable pageable);

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
}
