package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.projection.VideoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface OtherServicesTablesNativeQueryRepository extends JpaRepository<VideoPurchase, String> {
    @Query(value = "SELECT id, COALESCE(email, '') AS email, COALESCE(referral_grade, '') AS referralGrade, COALESCE(nickname, '') AS nickname, COALESCE(linked_stripe_id, '') AS linkedStripeId FROM users WHERE id = :userId", nativeQuery = true)
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
            "             AND (:searchString IS NULL OR u.email LIKE :searchString% OR u.nickname LIKE :searchString%)",
            countQuery = "SELECT COUNT(*) \n" +
                    "FROM  referrals r \n" +
                    "INNER JOIN  users u ON u.id = r.referred_pd_user_id \n" +
                    "INNER JOIN  earning ew ON ew.user_id = u.id  and ew.user_id = r.referred_pd_user_id \n" +
                    "WHERE r.referrer_pd_user_id COLLATE utf8mb4_unicode_ci = :pdUserId \n" +
                    "AND (:startDate IS NULL OR FROM_UNIXTIME(u.created_date) >= :startDate) \n" +
                    "            AND (:endDate IS NULL OR  FROM_UNIXTIME(u.created_date) <= :endDate) \n" +
                    "             AND (:searchString IS NULL OR u.email LIKE :searchString% OR u.nickname LIKE :searchString%)",
            nativeQuery = true)
    Page<Object[]> getListOfAllTheReferredPdsEOL(String pdUserId, LocalDate startDate, LocalDate endDate, String searchString, Pageable pageable);


    @Query(value =
            " SELECT " +
            "     COALESCE(u.nickname, ' ') AS nickname, " +
            "     COALESCE(u.email, ' ') AS email, " +
            "     COALESCE(FROM_UNIXTIME(u.created_date), ' ') AS created_date, " +
            "     COALESCE(u.pd_type, ' ') AS pd_type," +
            "     COALESCE(ew.trees_earned, 0.00) AS trees_earned, " +
            "     COALESCE(w.latest_withdrawal_date, ' ') AS withdrawal_date, " +
            "     COALESCE(w.trees, 0.00) AS trees_exchanged, " +
            "     COALESCE(w.leafs, 0.00) AS leaves_exchanged, " +
            "     COALESCE(ew.leafs_earned, 0.00) AS leafs_earned, " +
            "     COALESCE(r.referred_pd_user_id, ' ') AS userId " +
            " FROM referrals r " +
            " INNER JOIN users u ON u.id = r.referred_pd_user_id " +
            " LEFT JOIN earning ew ON ew.user_id = u.id AND ew.user_id = r.referred_pd_user_id " +
            " LEFT JOIN (" +
            "     SELECT " +
            "         wd.pd_user_id," +
            "         wd.trees," +
            "         wd.leafs," +
            "         wd.created_date AS latest_withdrawal_date" +
            "     FROM withdrawals wd" +
            "     INNER JOIN (" +
            "         SELECT " +
            "             pd_user_id, " +
            "             MAX(created_date) AS latest_withdrawal_date" +
            "         FROM withdrawals" +
            "         GROUP BY pd_user_id" +
            "     ) latest_wd ON wd.pd_user_id = latest_wd.pd_user_id AND wd.created_date = latest_wd.latest_withdrawal_date" +
            " ) w ON w.pd_user_id = r.referred_pd_user_id AND w.pd_user_id = u.id AND w.pd_user_id = ew.user_id" +
            " WHERE r.referrer_pd_user_id COLLATE utf8mb4_unicode_ci = :referrerPdUserId" +
            " AND (:searchString IS NULL OR u.email LIKE CONCAT(:searchString, '%') OR u.nickname LIKE CONCAT(:searchString, '%'))",
            countQuery =
                    " SELECT COUNT(*) FROM  referrals r " +
                    " INNER JOIN  users u ON u.id = r.referred_pd_user_id " +
                    " LEFT JOIN  earning ew ON ew.user_id = u.id  and ew.user_id = r.referred_pd_user_id " +
                    " LEFT JOIN  withdrawals w ON w.pd_user_id = r.referred_pd_user_id AND w.pd_user_id  = u.id AND w.pd_user_id  = ew.user_id" +
                    " where r.referrer_pd_user_id COLLATE utf8mb4_unicode_ci = :referrerPdUserId" +
                    " AND (:searchString IS NULL OR u.email LIKE CONCAT(:searchString, '%') OR u.nickname LIKE CONCAT(:searchString, '%'))",
            nativeQuery = true)
    Page<Object[]> getListOfAllTheReferredPds(String referrerPdUserId, String searchString, Pageable pageable);

    @Query(value = "SELECT rc.id as referralCommissionId, rc.withdrawal_id as withdrawalId, rc.referrer_pd_user_id as referrerPdUserId, " +
            "rc.commission_percent as commissionPercent, rc.commission_amount_in_trees as commissionAmountInTrees, " +
            "rc.commission_amount_in_cents as commissionAmountInCents, rc.created_date as referralCommissionCreatedDate, " +
            "rc.updated_date as referralCommissionUpdatedDate, rc.commission_transfer_status as commissionTransferStatus, " +
            "w.pd_user_id as withdrawalUserId, w.trees as withdrawalTrees, w.leafs withdrawalLeafs, w.status as withdrawalStatus, " +
            "w.created_date as withdrawalCreatedDate, w.updated_date as withdrawalUpdatedDate, " +
            "u.nickname as userNickname, u.pd_type as pdType, rc.referrer_referral_grade as referralGrade " +
            "FROM referral_commission rc " +
            "INNER JOIN withdrawals w ON rc.withdrawal_id = w.id " +
            "INNER JOIN users u ON w.pd_user_id = u.id " +
            "WHERE rc.referrer_pd_user_id = :referrerPdUserId " +
            "AND (:startDate IS NULL OR rc.created_date >= :startDate) " +
            "AND (:endDate IS NULL OR rc.created_date <= :endDate) " +
            "AND (COALESCE(:searchString, '') = '' OR u.nickname LIKE CONCAT(:searchString, '%')) " +
            " order by w.created_date desc",
            countQuery = "SELECT COUNT(*) FROM referral_commission rc " +
                    "INNER JOIN withdrawals w ON rc.withdrawal_id = w.id " +
                    "INNER JOIN users u ON w.pd_user_id = u.id " +
                    "WHERE rc.referrer_pd_user_id = :referrerPdUserId " +
                    "AND (:startDate IS NULL OR rc.created_date >= :startDate) " +
                    "AND (:endDate IS NULL OR rc.created_date <= :endDate) " +
                    "AND (COALESCE(:searchString, '') = '' OR u.nickname LIKE CONCAT(:searchString, '%'))",
            nativeQuery = true)
    Page<Object[]> getReferralCommissionDetailsWithFilters(
            @Param("referrerPdUserId") String referrerPdUserId,
            Pageable pageable,
            @Param("searchString") String searchString,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query( value =
            " SELECT group_concat(rc.id SEPARATOR ', ')                         AS referralCommissionId," +
            "        group_concat(rc.withdrawal_id SEPARATOR ', ')              AS withdrawalId," +
            "        rc.referrer_pd_user_id                      AS referrerPdUserId," +
            "        rc.commission_percent                       AS referrerCommissionPercent," +
            "        sum(rc.commission_amount_in_trees)          AS referrerCommissionAmountInTrees," +
            "        sum(rc.commission_amount_in_leafs)          AS referrerCommissionAmountInLeafs," +
            "        date(rc.created_date)                       AS referrerCommissionCreatedDate," +
            "        date(rc.updated_date)                       AS referrerCommissionUpdatedDate," +
            "        group_concat(rc.commission_transfer_status SEPARATOR ', ') AS referrerCommissionTransferStatus," +
            "        COALESCE(u.nickname, '')                    AS referrerUserNickname," +
            "        COALESCE(u.email, '')                       AS referrerUserEmail," +
            "        COALESCE(u.pd_type, '')                     AS referrerPdType," +
            "        COALESCE(u.linked_stripe_id, '')            AS linkedStripeId," +
            "        group_concat(w.pd_user_id)                  AS referredPdUserId," +
            "        COALESCE(rc.referrer_referral_grade, '')    AS referrerReferralGrade" +
            " FROM referral_commission rc" +
            "          INNER JOIN users u ON rc.referrer_pd_user_id = u.id" +
            "          INNER JOIN withdrawals w ON rc.withdrawal_id = w.id" +
            " WHERE DATE(w.created_date) = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) " +
            "   and w.status = 'COMPLETE' " +
            "   AND (:searchString IS NULL OR u.email LIKE CONCAT(:searchString, '%') OR u.nickname LIKE CONCAT(:searchString, '%')) " +
            " group by rc.referrer_pd_user_id;",
            countQuery =
                " select count(*)" +
                " from (SELECT rc.referrer_pd_user_id" +
                "       FROM referral_commission rc" +
                "                INNER JOIN users u ON rc.referrer_pd_user_id = u.id" +
                "                INNER JOIN withdrawals w ON rc.withdrawal_id = w.id" +
                "       WHERE DATE(w.created_date) = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)" +
                "         and w.status = 'COMPLETE'" +
                "         AND (:searchString IS NULL OR u.email LIKE CONCAT(:searchString, '%') OR" +
                "              u.nickname LIKE CONCAT(:searchString, '%'))" +
                "       group by rc.referrer_pd_user_id) as count",
            nativeQuery = true)
    Page<Object[]> getReferralCommissionHistoryForAdminDashboard(
            Pageable pageable,
            @Param("searchString") String searchString
    );

    @Query(value = "SELECT COUNT(*) FROM referrals WHERE referrer_pd_user_id = :referrerPdUserId", nativeQuery = true)
    Integer totalNumberOfReferredPdByCurrentPd(@Param("referrerPdUserId") String referrerPdUserId);

    @Query(value = "SELECT COUNT(DISTINCT referred_pd_user_id) FROM referrals", nativeQuery = true)
    Integer totalNumberOfReferredPdInSystem();

    @Query(value = "SELECT referrer_pd_user_id, COUNT(*) FROM referrals where referrer_pd_user_id in :userIdList GROUP BY referrer_pd_user_id", nativeQuery = true)
    List<Object[]> getReferralCounts(List<String> userIdList);

    default Map<String, Long> getReferralCountsMap(List<String> userIdList) {
        return getReferralCounts(userIdList).stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));
    }

    @Query(value =
            "SELECT " +
            "    COALESCE(u.id, '') AS referredPdUserId, " +
            "    COALESCE(u.email, '') AS referredPdUserEmail, " +
            "    COALESCE(u.nickname, '') AS referredPdUserNickname, " +
            "    COALESCE(e.trees_earned, 0) AS treesEarned, " +
            "    COALESCE(w.trees, 0) AS treesExchangedLatest, " +
            "    COALESCE(w.leafs, 0) AS leavesExchangedLatest, " +
            "    COALESCE(e.leafs_earned, 0) AS leavesEarned " +
            "FROM " +
            "    referrals r " +
            "JOIN " +
            "    users u ON r.referred_pd_user_id = u.id " +
            "LEFT JOIN " +
            "    earning e ON e.user_id = u.id " +
            "LEFT JOIN " +
            "    ( " +
            "        SELECT " +
            "            wd.pd_user_id, " +
            "            SUM(wd.trees) AS trees, " +
            "            SUM(wd.leafs) AS leafs, " +
            "            wd.created_date AS latest_withdrawal_date " +
            "        FROM " +
            "            withdrawals wd " +
            "            WHERE DATE(wd.created_date) = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) " +
            "            GROUP BY  wd.pd_user_id, wd.created_date " +
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


    @Query(value =
            " SELECT DISTINCT u.id,COALESCE(u.nickname, ' ') AS nickname, COALESCE(u.email, ' ') AS email, " +
            " COALESCE(ew.trees_earned, 0.00) AS trees_earned, " +
            " COALESCE(FROM_UNIXTIME(u.created_date), ' ') AS created_date, " +
            " ref_code.referral_code, COALESCE(u.referral_grade, 'GENERAL') " +
            " FROM referrals r " +
            " INNER JOIN users u ON u.id = r.referrer_pd_user_id " +
            " INNER JOIN earning ew ON ew.user_id = u.id AND ew.user_id = r.referrer_pd_user_id " +
            " LEFT JOIN (SELECT COALESCE(referral_code, '') AS referral_code, id FROM users) AS ref_code ON ref_code.id = u.id " +
            " WHERE (:pdUserId IS NULL OR r.referred_pd_user_id COLLATE utf8mb4_unicode_ci = :pdUserId) " +
            " AND (:startDate IS NULL OR FROM_UNIXTIME(u.created_date) >= :startDate) " +
            " AND (:endDate IS NULL OR FROM_UNIXTIME(u.created_date) <= :endDate) " +
            " AND (:searchString IS NULL OR :searchString = '' OR u.email LIKE CONCAT(:searchString, '%') OR u.nickname LIKE CONCAT(:searchString, '%'))" +
            "",
            countQuery =
                    " select count(*) from (SELECT DISTINCT u.id,COALESCE(u.nickname, ' ') AS nickname, COALESCE(u.email, ' ') AS email, COALESCE(ew.trees_earned, 0.00) AS trees_earned, COALESCE(FROM_UNIXTIME(u.created_date), ' ') AS created_date, ref_code.referral_code, COALESCE(u.referral_grade, 'GENERAL')" +
                    " FROM referrals r " +
                    " INNER JOIN users u ON u.id = r.referrer_pd_user_id " +
                    " INNER JOIN earning ew ON ew.user_id = u.id AND ew.user_id = r.referrer_pd_user_id " +
                    " LEFT JOIN (SELECT COALESCE(referral_code, '') AS referral_code, id FROM users) AS ref_code ON ref_code.id = u.id " +
                    " WHERE (:pdUserId IS NULL OR r.referred_pd_user_id COLLATE utf8mb4_unicode_ci = :pdUserId) " +
                    " AND (:startDate IS NULL OR FROM_UNIXTIME(u.created_date) >= :startDate) " +
                    " AND (:endDate IS NULL OR FROM_UNIXTIME(u.created_date) <= :endDate) " +
                    " AND (:searchString IS NULL OR :searchString = '' OR u.email LIKE CONCAT(:searchString, '%') OR u.nickname LIKE CONCAT(:searchString, '%'))"+
                    ") as temp",
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

    @Query(value = "SELECT nickname from users where id = :userId", nativeQuery = true)
    Optional<String> getNicknameByUserId(@Param("userId") String userId);


    @Query(value = "SELECT video_id as videoId, user_id as userId, drm_enable as drmEnable, video_library_id as videoLibraryId FROM videos WHERE video_id = :videoId", nativeQuery = true)
    Optional<VideoProjection> findUserIdByVideoId(@Param("videoId") String videoId);

    @Query(value = "select video_id, duration, trees, enabled from video_duration_price where video_id = :videoId", nativeQuery = true)
    List<Object[]> findPricesByVideoId(@Param("videoId") String videoId);

    @Query(value = "select target_user_id " +
            "from block_user where blocker_user_id = :userid", nativeQuery = true)
    List<String> findBlockedUsersByUserId(@Param("userid") String userId);

    @Query(value = "SELECT u.id FROM users u WHERE u.email = :email",nativeQuery = true)
    String findUserIdByEmail(@Param("email") String email);

    @Query(value = "SELECT u.email, u.nickname FROM users u WHERE u.id = :userId", nativeQuery = true)
    List<Object[]> findEmailAndNicknameByUserId(@Param("userId") String userId);

    @Query(value = "SELECT email FROM users WHERE id = :userId", nativeQuery = true)
    Optional<String> findEmailByUserId(@Param("userId") String userId);

    @Query(
        value =
        " SELECT COUNT(*) > 0 AS is_exist" +
        " FROM user_followings" +
        " WHERE follower = :userId and is_deleted = false", nativeQuery = true)
    Long isFollowingExists(String userId);

    @Query(value =
        " SELECT distinct following" +
        " FROM user_followings" +
        " WHERE follower = :userId" +
        "   and following in :pdIds", nativeQuery = true)
    List<String> findFollowingByListPd(String userId, Set<String> pdIds);

    @Query(value = "select count(*) from user_followings uf where following = :pdId and uf.is_deleted=false", nativeQuery = true)
    Long getFollowersCount(String pdId);
}
