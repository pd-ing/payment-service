package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferredPdDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OtherServicesTablesNativeQueryRepository extends JpaRepository<VideoPurchase, String> {
    @Query(value = "SELECT id, COALESCE(email, '') AS email, COALESCE(pd_type, '') AS pdType, COALESCE(nickname, '') AS nickname, COALESCE(linked_stripe_id, '') AS linkedStripeId FROM users WHERE id = :userId", nativeQuery = true)
    List<Object[]> findUserInfoByUserId(@Param("userId") String userId);

    @Query(value = "SELECT * FROM referrals WHERE referred_pd_user_id = :referredPdUserId", nativeQuery = true)
    List<Object[]> findReferralDetailsByReferredPdUserId(@Param("referredPdUserId") String referredPdUserId);


    @Query(value = "SELECT COALESCE(u.pd_type, '') \n" +
            " FROM users u INNER JOIN referrals r ON r.referrer_pd_user_id = u.id \n" +
            " WHERE referred_pd_user_id COLLATE utf8mb4_unicode_ci = :referredPdUserId", nativeQuery = true)
    String getReferralPdGrade(@Param("referredPdUserId") String referredPdUserId);

    @Query(value = "SELECT COALESCE(u.id, '') \n" +
            " FROM users u WHERE email = :email", nativeQuery = true)
    String getUserIdFromEmail(@Param("email") String email);

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
            "COALESCE(FROM_UNIXTIME(u.created_date), ' ') AS created_date, \n" +
            "COALESCE(u.pd_type, ' ') \n" +
            "COALESCE(ew.trees_earned, 0.00) AS trees_earned \n" +
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
    Page<Object[]> getListOfAllTheReferredPdsEOL(String pdUserId, LocalDate startDate, LocalDate endDate, String searchString, Pageable pageable);


    @Query(value = "SELECT \n" +
            "    COALESCE(u.nickname, ' ') AS nickname, \n" +
            "    COALESCE(u.email, ' ') AS email, \n" +
            "    COALESCE(FROM_UNIXTIME(u.created_date), ' ') AS created_date, \n" +
            "    COALESCE(u.pd_type, ' ') AS pd_type,\n" +
            "    COALESCE(ew.trees_earned, 0.00) AS trees_earned, \n" +
            "    COALESCE(w.latest_withdrawal_date, ' ') AS withdrawal_date, \n" +
            "    COALESCE(w.trees, 0.00) AS trees_exchanged, \n" +
            "    COALESCE(w.leafs, 0.00) AS leaves_exchanged, \n" +
            "    COALESCE(ew.leafs_earned, 0.00) AS leafs_earned, \n" +
            "    COALESCE(r.referred_pd_user_id, ' ') AS userId \n" +
            "FROM referrals r \n" +
            "INNER JOIN users u ON u.id = r.referred_pd_user_id \n" +
            "LEFT JOIN earning ew ON ew.user_id = u.id AND ew.user_id = r.referred_pd_user_id \n" +
            "LEFT JOIN (\n" +
            "    SELECT \n" +
            "        wd.pd_user_id,\n" +
            "        wd.trees,\n" +
            "        wd.leafs,\n" +
            "        wd.created_date AS latest_withdrawal_date\n" +
            "    FROM withdrawals wd\n" +
            "    INNER JOIN (\n" +
            "        SELECT \n" +
            "            pd_user_id, \n" +
            "            MAX(created_date) AS latest_withdrawal_date\n" +
            "        FROM withdrawals\n" +
            "        GROUP BY pd_user_id\n" +
            "    ) latest_wd ON wd.pd_user_id = latest_wd.pd_user_id AND wd.created_date = latest_wd.latest_withdrawal_date\n" +
            ") w ON w.pd_user_id = r.referred_pd_user_id AND w.pd_user_id = u.id AND w.pd_user_id = ew.user_id\n" +
            "WHERE r.referrer_pd_user_id COLLATE utf8mb4_unicode_ci = :referrerPdUserId",
            countQuery = "SELECT COUNT(*) FROM  referrals r \n" +
                    "INNER JOIN  users u ON u.id = r.referred_pd_user_id \n" +
                    "LEFT JOIN  earning ew ON ew.user_id = u.id  and ew.user_id = r.referred_pd_user_id \n" +
                    "LEFT JOIN  withdrawals w ON w.pd_user_id = r.referred_pd_user_id AND w.pd_user_id  = u.id AND w.pd_user_id  = ew.user_id\n" +
                    "where r.referrer_pd_user_id COLLATE utf8mb4_unicode_ci = :referrerPdUserId",
            nativeQuery = true)
    Page<Object[]> getListOfAllTheReferredPds(String referrerPdUserId, Pageable pageable);

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
            "rc.commission_amount_in_leafs AS referrerCommissionAmountInLeafs, " +
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
            "WHERE DATE(w.created_date) = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)" +
            "AND (:searchString IS NULL " +
            "       OR u.email LIKE %:searchString% " +
            "       OR u.nickname LIKE %:searchString%) " +
            "ORDER BY " +
            "CASE WHEN rc.commission_transfer_status = 'TRANSFER_PENDING' THEN 0 ELSE 1 END, " +
            "rc.updated_date ASC",
            countQuery = "SELECT COUNT(*) FROM referral_commission rc " +
                    "INNER JOIN users u ON rc.referrer_pd_user_id = u.id " +
                    "INNER JOIN withdrawals w ON rc.withdrawal_id = w.id " +
                    "WHERE DATE(w.created_date) = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) " +
                    "AND (:searchString IS NULL " +
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

    @Query(value = "SELECT \n" +
            "    COALESCE(u.id, '') AS referredPdUserId, \n" +
            "    COALESCE(u.email, '') AS referredPdUserEmail, \n" +
            "    COALESCE(u.nickname, '') AS referredPdUserNickname, \n" +
            "    COALESCE(e.trees_earned, 0) AS treesEarned, \n" +
            "    COALESCE(w.trees, 0) AS treesExchangedLatest, \n" +
            "    COALESCE(w.leafs, 0) AS leavesExchangedLatest, \n" +
            "    COALESCE(e.leafs_earned, 0) AS leavesEarned \n" +
            "FROM \n" +
            "    referrals r \n" +
            "JOIN \n" +
            "    users u ON r.referred_pd_user_id = u.id \n" +
            "LEFT JOIN \n" +
            "    earning e ON e.user_id = u.id \n" +
            "LEFT JOIN \n" +
            "    ( \n" +
            "        SELECT \n" +
            "            wd.pd_user_id, \n" +
            "            SUM(wd.trees) AS trees, \n" +
            "            SUM(wd.leafs) AS leafs, \n" +
            "            wd.created_date AS latest_withdrawal_date \n" +
            "        FROM \n" +
            "            withdrawals wd \n" +
            "            WHERE DATE(wd.created_date) = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) \n" +
            "            GROUP BY  wd.pd_user_id, wd.created_date \n" +
            "    ) w ON w.pd_user_id = r.referred_pd_user_id AND w.pd_user_id = u.id AND w.pd_user_id = e.user_id " +
            " WHERE r.referrer_pd_user_id COLLATE utf8mb4_unicode_ci = :referrerPdUserId",
            countQuery = "SELECT COUNT(*) " +
                    "FROM referrals r " +
                    "JOIN users u ON r.referred_pd_user_id = u.id " +
                    "LEFT JOIN (SELECT user_id, SUM(trees_earned) AS trees_earned FROM earning GROUP BY user_id) e ON e.user_id = u.id " +
                    "LEFT JOIN (SELECT pd_user_id, SUM(trees) AS total_trees_withdrawn FROM withdrawals GROUP BY pd_user_id) w ON w.pd_user_id = u.id " +
                    "WHERE r.referrer_pd_user_id = :referrerPdUserId",
            nativeQuery = true)
    Page<Object[]> getReferredPdDetails(@Param("referrerPdUserId") String referrerPdUserId, Pageable pageable);


    @Query(value = "SELECT DISTINCT u.id,COALESCE(u.nickname, ' ') AS nickname, COALESCE(u.email, ' ') AS email, \n" +
            "COALESCE(ew.trees_earned, 0.00) AS trees_earned, \n" +
            "COALESCE(FROM_UNIXTIME(u.created_date), ' ') AS created_date, \n" +
            "ref_code.referral_code, COALESCE(u.referral_grade, 'GENERAL') \n" +
            "FROM referrals r \n" +
            "INNER JOIN users u ON u.id = r.referrer_pd_user_id \n" +
            "INNER JOIN earning ew ON ew.user_id = u.id AND ew.user_id = r.referrer_pd_user_id \n" +
            "LEFT JOIN (SELECT COALESCE(referral_code, '') AS referral_code, id FROM users) AS ref_code ON ref_code.id = u.id \n" +
            "WHERE (:pdUserId IS NULL OR r.referred_pd_user_id COLLATE utf8mb4_unicode_ci = :pdUserId) \n" +
            "AND (:startDate IS NULL OR FROM_UNIXTIME(u.created_date) >= :startDate) \n" +
            "AND (:endDate IS NULL OR FROM_UNIXTIME(u.created_date) <= :endDate) \n" +
            "AND (:searchString IS NULL OR u.email LIKE %:searchString% OR u.nickname LIKE %:searchString%)",
            countQuery = "SELECT COUNT(*) \n" +
                    "FROM referrals r \n" +
                    "INNER JOIN users u ON u.id = r.referrer_pd_user_id \n" +
                    "INNER JOIN earning ew ON ew.user_id = u.id AND ew.user_id = r.referrer_pd_user_id \n" +
                    "WHERE (:pdUserId IS NULL OR r.referred_pd_user_id COLLATE utf8mb4_unicode_ci = :pdUserId) \n" +
                    "AND (:startDate IS NULL OR FROM_UNIXTIME(u.created_date) >= :startDate) \n" +
                    "AND (:endDate IS NULL OR FROM_UNIXTIME(u.created_date) <= :endDate) \n" +
                    "AND (:searchString IS NULL OR u.email LIKE %:searchString% OR u.nickname LIKE %:searchString%)",
            nativeQuery = true)
    Page<Object[]> getListOfAllTheReferrerPds(String pdUserId, LocalDate startDate, LocalDate endDate, String searchString, Pageable pageable);


    @Query(value = "SELECT SUM(e.trees_earned) as total_trees_earned " +
            "FROM referrals r " +
            "LEFT JOIN earning e ON r.referred_pd_user_id = e.user_id " +
            "WHERE r.referrer_pd_user_id = :referrerPdUserId", nativeQuery = true)
    BigDecimal getTotalTreesEarnedByReferredPdUsers(@Param("referrerPdUserId") String referrerPdUserId);

    @Query(value = "SELECT SUM(e.leafs_earned) as total_leafs_earned " +
            "FROM referrals r " +
            "LEFT JOIN earning e ON r.referred_pd_user_id = e.user_id " +
            "WHERE r.referrer_pd_user_id = :referrerPdUserId", nativeQuery = true)
    BigDecimal getTotalLeafsEarnedByReferredPdUsers(@Param("referrerPdUserId") String referrerPdUserId);

    @Query(value = "SELECT language FROM user_config WHERE id = :userId", nativeQuery = true)
    Optional<String> findLanguageById(@Param("userId") String userId);

    @Query(value = "SELECT id from users where uuid = :uuid", nativeQuery = true)
    Optional<String> getUserIdByUUID(@Param("uuid") String uuid);
}
