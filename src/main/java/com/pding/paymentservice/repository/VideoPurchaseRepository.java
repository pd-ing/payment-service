package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.tables.inner.VideoEarningsAndSales;
import com.pding.paymentservice.payload.projection.MonthlyRevenueProjection;
import com.pding.paymentservice.payload.projection.UserProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface VideoPurchaseRepository extends JpaRepository<VideoPurchase, String> {
    boolean existsByVideoId(String videoId);

    List<VideoPurchase> getVideoPurchaseByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT vp from VideoPurchase vp where vp.userId = :userId and vp.videoId = :videoId and vp.isRefunded = false")
    List<VideoPurchase> findByUserIdAndVideoIdSelectForUpdate(String userId, String videoId);

    @Query("SELECT vp from VideoPurchase vp where vp.userId = :userId and vp.videoId = :videoId and vp.isRefunded = false")
    List<VideoPurchase> findByUserIdAndVideoId(String userId, String videoId);

    @Query("SELECT vp.videoId, COALESCE(SUM(vp.treesConsumed), 0) - COALESCE(SUM(vp.drmFee), 0), COUNT(vp) FROM VideoPurchase vp WHERE vp.videoId IN :videoIds and vp.isRefunded = false GROUP BY vp.videoId")
    List<Object[]> getTotalTreesEarnedAndSalesCountForVideoIds(@Param("videoIds") List<String> videoIds);

    default Map<String, VideoEarningsAndSales> getTotalTreesEarnedAndSalesCountMapForVideoIds(List<String> videoIds) {
        List<Object[]> results = getTotalTreesEarnedAndSalesCountForVideoIds(videoIds);

        // Create a map with default values for cases where a record is not found
        Map<String, VideoEarningsAndSales> resultMap = new HashMap<>();
        for (String videoId : videoIds) {
            resultMap.put(videoId, new VideoEarningsAndSales(BigDecimal.ZERO, 0L));
        }

        // Update values for records that are found
        results.forEach(result -> {
            String videoId = (String) result[0];
            BigDecimal totalTreesEarned = (BigDecimal) result[1];
            Long totalSalesCount = (Long) result[2];
            resultMap.put(videoId, new VideoEarningsAndSales(totalTreesEarned, totalSalesCount));
        });

        return resultMap;
    }

    @Query("SELECT vp from VideoPurchase vp where vp.videoId = :videoId and vp.isRefunded = false and vp.packagePurchaseId is null order by vp.lastUpdateDate desc")
    Page<VideoPurchase> findAllByVideoIdOrderByLastUpdateDateDesc(String videoId, Pageable pageable);

    @Query("SELECT vp from VideoPurchase vp where vp.videoId = :videoId and vp.userId in :onlyTheseUsersList and vp.isRefunded = false and vp.packagePurchaseId is null order by vp.lastUpdateDate desc")
    Page<VideoPurchase> findAllByVideoIdAndUserIdInOrderByLastUpdateDateDesc(String videoId, List<String> onlyTheseUsersList, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(vp.treesConsumed), 0) FROM VideoPurchase vp WHERE vp.userId = :userId and vp.isRefunded = false")
    BigDecimal getTotalTreesConsumedByUserId(@Param("userId") String userId);

    @Query(value =
                "SELECT COALESCE(purchase_date, '')  as last_update_date," +
                "       COALESCE(title, '')          as title," +
                "       COALESCE(trees_consumed, '') as trees_consumed," +
                "       COALESCE(email, '')          as email," +
                "       COALESCE(nickname, '')       as nickname," +
                "       COALESCE(duration, '')       as duration," +
                "       COALESCE(expiry_date, '')    as expiry_date," +
                "       COALESCE(type, '')    as type," +
                "       COALESCE(number_of_videos, '')    as number_of_videos," +
                "       discount_percentage" +
                " FROM (" +
                "         SELECT vp.last_update_date as purchase_date," +
                "                v.title," +
                "                vp.trees_consumed," +
                "                buyer.email," +
                "                buyer.nickname," +
                "                vp.duration," +
                "                vp.expiry_date," +
                "                'SINGLE'            as type," +
                "                1                   as number_of_videos," +
                "                0 as discount_percentage" +
                "         FROM video_purchase vp" +
                "                  LEFT JOIN videos v ON vp.video_id = v.video_id" +
                "                  LEFT JOIN users buyer ON vp.user_id = buyer.id" +
                "         WHERE vp.video_owner_user_id = :userId" +
                "           AND vp.is_refunded = false" +
                "           AND vp.package_purchase_id is null" +
                "         UNION ALL" +
                "         SELECT vpp.purchase_date," +
                "                vp.title," +
                "                vpp.trees_consumed," +
                "                buyer.email," +
                "                buyer.nickname," +
                "                'PERMANENT'     as duration," +
                "                NULL            as expiry_date," +
                "                vp.package_type as type," +
                "                (LENGTH(vpp.included_video_ids) - LENGTH(REPLACE(vpp.included_video_ids, ',', '')) + 1) as number_of_videos," +
                "                vpp.discount_percentage as discount_percentage" +
                "         FROM video_package_purchase vpp" +
                "                  LEFT JOIN video_packages vp ON vpp.package_id = vp.id" +
                "                  LEFT JOIN users buyer ON vpp.user_id = buyer.id" +
                "         WHERE vp.seller_id = :userId" +
                "           AND vpp.is_refunded = false) combined_purchases" +
                " WHERE (:searchString IS NULL" +
                "    OR email LIKE CONCAT(:searchString, '%')" +
                "    OR title LIKE CONCAT(:searchString, '%')" +
                "    )" +
                "  AND (:startDate IS NULL OR purchase_date >= :startDate)" +
                "  AND (:endDate IS NULL OR purchase_date < :endDate)",
            countQuery = "SELECT count(*)" +
                " FROM (" +
                "         SELECT vp.last_update_date as purchase_date," +
                "                v.title," +
                "                vp.trees_consumed," +
                "                buyer.email," +
                "                vp.duration," +
                "                vp.expiry_date," +
                "                'SINGLE'            as type," +
                "                1                   as number_of_videos" +
                "         FROM video_purchase vp" +
                "                  LEFT JOIN videos v ON vp.video_id = v.video_id" +
                "                  LEFT JOIN users buyer ON vp.user_id = buyer.id" +
                "         WHERE vp.video_owner_user_id = :userId" +
                "           AND vp.is_refunded = false" +
                "           AND vp.package_purchase_id is null" +
                "         UNION ALL" +
                "         SELECT vpp.purchase_date," +
                "                vp.title," +
                "                vpp.trees_consumed," +
                "                buyer.email," +
                "                'PERMANENT'     as duration," +
                "                NULL            as expiry_date," +
                "                vp.package_type as type," +
                "                (LENGTH(vpp.included_video_ids) - LENGTH(REPLACE(vpp.included_video_ids, ',', '')) + 1) as number_of_videos" +
                "         FROM video_package_purchase vpp" +
                "                  LEFT JOIN video_packages vp ON vpp.package_id = vp.id" +
                "                  LEFT JOIN users buyer ON vpp.user_id = buyer.id" +
                "         WHERE vp.seller_id = :userId" +
                "           AND vpp.is_refunded = false) combined_purchases" +
                " WHERE (:searchString IS NULL" +
                "    OR email LIKE CONCAT(:searchString, '%')" +
                "    OR title LIKE CONCAT(:searchString, '%')" +
                "    )" +
                "  AND (:startDate IS NULL OR purchase_date >= :startDate)" +
                "  AND (:endDate IS NULL OR purchase_date < :endDate)",
            nativeQuery = true)
    Page<Object[]> getSalesHistoryByUserIdAndDates(String searchString, String userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

//    @Query(value =
//            "SELECT COALESCE(vp.last_update_date, ''), " +
//                    "COALESCE(vp.trees_consumed, ''), " +
//                    "COALESCE(u.email, ''), " +
//                    "COALESCE(vp.duration, ''), " +
//                    "COALESCE(DATE_FORMAT(vp.expiry_date, '%Y-%m-%d %H:%i:%s'), '') " +
//                    "FROM video_purchase vp " +
//                    "LEFT JOIN videos v ON vp.video_id = v.video_id " +
//                    "LEFT JOIN users u ON vp.user_id = u.id " +
//                    "WHERE vp.video_owner_user_id = :userId and vp.is_refunded = false and vp.package_purchase_id is null " +
//                    "AND (:searchString IS NULL OR u.email like concat('%', :searchString, '%') OR v.title like concat('%', :searchString, '%')) " +
//                    "AND (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
//                    "AND (:endDate IS NULL OR vp.last_update_date < :endDate)"+
//                    "ORDER BY CASE WHEN :sortDirection = 'ASC' THEN vp.last_update_date END ASC, " +
//                    "CASE WHEN :sortDirection = 'DESC' THEN vp.last_update_date END DESC",
//            nativeQuery = true)
    @Query(value =
            "SELECT COALESCE(purchase_date, '')  as last_update_date," +
            "       COALESCE(trees_consumed, '') as trees_consumed," +
            "       COALESCE(email, '')          as email," +
            "       COALESCE(nickname, '')       as nickname," +
            "       COALESCE(duration, '')       as duration," +
            "       COALESCE(type, '')    as type," +
            "       COALESCE(number_of_videos, '')    as number_of_videos," +
            "       discount_percentage" +
            " FROM (" +
            "         SELECT vp.last_update_date as purchase_date," +
            "                v.title," +
            "                vp.trees_consumed," +
            "                buyer.email," +
            "                buyer.nickname," +
            "                vp.duration," +
            "                'SINGLE'            as type," +
            "                1                   as number_of_videos," +
            "                0 as discount_percentage" +
            "         FROM video_purchase vp" +
            "                  LEFT JOIN videos v ON vp.video_id = v.video_id" +
            "                  LEFT JOIN users buyer ON vp.user_id = buyer.id" +
            "         WHERE vp.video_owner_user_id = :userId" +
            "           AND vp.is_refunded = false" +
            "           AND vp.package_purchase_id is null" +
            "         UNION ALL" +
            "         SELECT vpp.purchase_date," +
            "                vp.title," +
            "                vpp.trees_consumed," +
            "                buyer.email," +
            "                buyer.nickname," +
            "                'PERMANENT'     as duration," +
//            "                ''            as expiry_date," +
            "                vp.package_type as type," +
            "                (LENGTH(vpp.included_video_ids) - LENGTH(REPLACE(vpp.included_video_ids, ',', '')) + 1) as number_of_videos," +
            "                vpp.discount_percentage as discount_percentage" +
            "         FROM video_package_purchase vpp" +
            "                  LEFT JOIN video_packages vp ON vpp.package_id = vp.id" +
            "                  LEFT JOIN users buyer ON vpp.user_id = buyer.id" +
            "         WHERE vp.seller_id = :userId" +
            "           AND vpp.is_refunded = false) combined_purchases" +
            " WHERE (:searchString IS NULL" +
            "    OR email LIKE CONCAT(:searchString, '%')" +
            "    OR title LIKE CONCAT(:searchString, '%')" +
            "    )" +
            "  AND (:startDate IS NULL OR purchase_date >= :startDate)" +
            "  AND (:endDate IS NULL OR purchase_date < :endDate)" +
            " ORDER BY CASE WHEN :sortDirection = 'ASC' THEN last_update_date END ASC, " +
            " CASE WHEN :sortDirection = 'DESC' THEN last_update_date END DESC",
        nativeQuery = true)
    List<Object[]> getAllSalesHistoryByUserIdAndDates(String searchString, String userId, LocalDate startDate, LocalDate endDate, String sortDirection);

    @Query(value =
            "SELECT COALESCE(SUM(vp.trees_consumed), 0) - COALESCE(SUM(drm_fee), 0)" +
            "FROM video_purchase vp " +
            "WHERE vp.video_owner_user_id = :userId and vp.is_refunded = false and vp.package_purchase_id is null " +
            "AND (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR vp.last_update_date < :endDate) ",
            nativeQuery = true)
   Long getTotalTreesEarned(String userId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT DISTINCT uf.follower, vp.user_id \n" +
            "FROM user_followings uf LEFT join video_purchase vp \n" +
            "ON uf.follower = vp.user_id\n" +
            "WHERE uf.is_deleted = FALSE and uf.following = :userId and vp.is_refunded = false",
            nativeQuery = true)
    List<Object[]> getFollowersList(String userId);

    @Query(value =
        " select count(distinct uf.follower)" +
            " from user_followings uf" +
            " left join video_purchase vp on vp.user_id = uf.follower and vp.video_owner_user_id = :pdId and vp.is_refunded = false" +
            " left join donation d on d.donor_user_id = uf.follower and d.pd_user_id = :pdId" +
            " left join call_purchase cp on cp.user_id = uf.follower and cp.pd_user_id = :pdId" +
            " left join message_purchase mp on mp.user_id = uf.follower and mp.pd_userid = :pdId" +
            " left join in_chat_media_trading mt on mt.user_id = uf.follower and mt.pd_id = :pdId and mt.transaction_status = 'PAID'" +
            " where uf.following = :pdId" +
            "   and uf.is_deleted = false" +
            "   and vp.user_id is null" +
            "   and d.donor_user_id is null" +
            "   and cp.user_id is null" +
            "   and mp.user_id is null" +
            "   and mt.user_id is null",
        nativeQuery = true)
    Long getUnPaidFollowersCount(String pdId);

    @Query(value = "SELECT COALESCE(SUM(vt.trees_consumed), 0) - COALESCE(SUM(vt.drm_fee), 0) FROM video_purchase vt WHERE vt.video_owner_user_id = ?1 AND vt.last_update_date >= DATE_SUB(?2, INTERVAL 24 HOUR) and vp.is_refunded = false", nativeQuery = true)
    BigDecimal getDailyTreeRevenueByVideoOwner(String videoOwnerUserId, LocalDateTime endDateTime);

    @Query("SELECT DISTINCT vp.videoOwnerUserId FROM VideoPurchase vp WHERE vp.userId = ?1 and vp.isRefunded != true and vp.expiryDate > current_time ")
    Page<String> getAllPdUserIdWhoseVideosArePurchasedByUser(String userId, Pageable pageable);

    @Query(value =
        " select pd.id," +
        "        pd.nickname        as displayName," +
        "        pd.description," +
        "        pd.profile_picture as profilePicture," +
        "        pd.profile_id      as profileId," +
        "        pd.pd_category     as pdCategory" +
        " from video_purchase vp" +
        "          left join users pd on vp.video_owner_user_id = pd.id" +
        " where vp.user_id = :userId" +
        "   and vp.is_refunded = false" +
        "   and vp.expiry_date > now()" +
        "   and (:searchString is null or pd.nickname like CONCAT(:searchString, '%'))" +
        " group by pd.id", nativeQuery = true)
    Page<UserProjection> getAllPdUserIdWhoseVideosArePurchasedByUserWithSearch(@Param("userId") String userId, @Param("searchString") String searchString, Pageable pageable);

    @Query(value =
            " select pd.id," +
            "        pd.nickname        as displayName," +
            "        pd.description," +
            "        pd.profile_picture as profilePicture," +
            "        pd.profile_id      as profileId," +
            "        pd.pd_category     as pdCategory" +
            " from video_purchase vp" +
            "          left join users pd on vp.video_owner_user_id = pd.id" +
            " where vp.user_id = :userId" +
            "   and vp.is_refunded = false" +
            "   and vp.expiry_date < now()" +
            "   and (:searchString is null or pd.nickname like CONCAT(:searchString, '%'))" +
            " group by pd.id" +
            " having max(vp.expiry_date) < now()"
        , nativeQuery = true)
    Page<UserProjection> getAllPdUserIdWhoseVideosAreExpiredByUserWithSearch(@Param("userId") String userId, @Param("searchString") String searchString, Pageable pageable);


    @Query("SELECT DISTINCT vp.videoOwnerUserId FROM VideoPurchase vp WHERE vp.userId = ?1 and vp.expiryDate < current_time and vp.isRefunded = false group by vp.videoId having max(vp.expiryDate) < current_time ")
    Page<String> getAllPdUserIdWhoseVideosAreExpiredByUser(String userId, Pageable pageable);

    @Query("SELECT vp from VideoPurchase vp where vp.userId = :userId and vp.videoId in :videoIds and vp.isRefunded = false ")
    List<VideoPurchase> findByUserIdAndVideoIdIn(String userId, Set<String> videoIds);

    @Query("SELECT vp from VideoPurchase vp where vp.userId = :userId and vp.isRefunded = false")
    List<VideoPurchase> findByUserId(String userId);

    @Query("SELECT vp from VideoPurchase vp where vp.userId = :userId and vp.videoOwnerUserId = :pdId and vp.isRefunded = false")
    List<VideoPurchase> findByUserIdAndPdId(String userId, String pdId);

    @Query(value = "select vp from VideoPurchase vp where vp.expiryDate > current_date and vp.userId = :userId and (:ownerId is null or vp.videoOwnerUserId = :ownerId) and vp.isRefunded = false")
    Page<VideoPurchase> findNotExpiredVideo(@Param("userId") String userId, @Param("ownerId") String ownerId, Pageable pageable);


    @Query(value =
            " select *, vp.last_update_date as lastUpdateDate, max(vp.expiry_date) as maxExpiryDate" +
            " from video_purchase vp" +
            " where 1 = 1 and vp.is_refunded = false" +
            "   and user_id = :userId" +
            "   and (:ownerId is null" +
            "     or video_owner_user_id = :ownerId)" +
            " group by video_id, user_id, video_owner_user_id" +
            " having maxExpiryDate < now()", nativeQuery = true)
    Page<VideoPurchase> findExpiredVideoPurchases(@Param("userId") String userId, @Param("ownerId") String ownerId, Pageable pageable);

    @Query("select count(vp) from VideoPurchase vp where vp.videoId = :videoId and vp.isRefunded = false")
    Long countByVideoId(String videoId);

    @Query(value = "select count(distinct user_id)" +
            " from video_purchase" +
            " where video_id = :videoId and is_refunded = false", nativeQuery = true)
    Long countUserBuyVideo(@Param("videoId") String videoId);

    @Query(value = " select vp.video_id," +
                   "        vp.video_owner_user_id," +
                   "        buyer.email," +
                   "        buyer.id," +
                   "        count(vp.id) as numberOfPurchases," +
                   "        group_concat(vp.last_update_date)," +
                   "        group_concat(vp.duration)," +
                   "        group_concat(vp.expiry_date)," +
                   "        group_concat(vp.trees_consumed)" +
                   " from video_purchase vp" +
                   "          join users buyer on vp.user_id = buyer.id" +
                   " where vp.video_id = :videoId and vp.is_refunded = false" +
                   " group by vp.video_id, vp.user_id", nativeQuery = true)
    Page<Object[]> getSaleHistoryByVideoId(@Param("videoId") String videoId, Pageable pageable);


    @Query(value = "SELECT vp from VideoPurchase vp where vp.videoOwnerUserId = :videoOwnerUserId and vp.isRefunded = false and vp.lastUpdateDate >= :startDate and vp.lastUpdateDate <= :endDate")
    List<VideoPurchase> getVideoPurchasesByVideoOwnerUserIdAndDates(String videoOwnerUserId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value =
                " select count(distinct uf.follower)" +
                " from user_followings uf" +
                " where uf.following = :pdId" +
                "   and uf.is_deleted = false" +
                "   and (" +
                "     exists (" +
                "       select 1" +
                "       from video_purchase vp" +
                "       where vp.user_id = uf.follower" +
                "         and vp.video_owner_user_id = :pdId" +
                "         and vp.is_refunded = false" +
                "     )" +
                "     or exists (" +
                "       select 1" +
                "       from donation d" +
                "       where d.donor_user_id = uf.follower" +
                "         and d.pd_user_id = :pdId" +
                "     )" +
                "     or exists (" +
                "       select 1" +
                "       from call_purchase cp" +
                "       where cp.user_id = uf.follower" +
                "         and cp.pd_user_id = :pdId" +
                "     )" +
                "     or exists (" +
                "       select 1" +
                "       from message_purchase mp" +
                "       where mp.user_id = uf.follower" +
                "         and mp.pd_userid = :pdId" +
                "     )" +
                "     or exists (" +
                "       select 1" +
                "       from in_chat_media_trading mt" +
                "       where mt.user_id = uf.follower" +
                "         and mt.pd_id = :pdId" +
                "         and mt.transaction_status = 'PAID'" +
                "     )" +
                "   )",
        nativeQuery = true)
    Long getPaidFollowersCount(String pdId);

    @Query(nativeQuery = true, value =
        " SELECT DATE_FORMAT(vp.last_update_date, '%Y-%m') AS month," +
            "        COALESCE(SUM(vp.trees_consumed), 0)                    as revenue" +
            " FROM video_purchase vp" +
            " WHERE vp.video_owner_user_id = :pdId" +
//            "   AND vp.last_update_date >= DATE_SUB(CURRENT_DATE, INTERVAL 3 MONTH)" +
            " GROUP BY DATE_FORMAT(vp.last_update_date, '%Y-%m')" +
            " ORDER BY month DESC" +
            " LIMIT :limit"
    )
    List<MonthlyRevenueProjection> getMonthlyRevenueFromVideoPurchaseByUserId(@Param("pdId") String pdId, @Param("limit") Integer limit);




    //TODO check if is_refunded is set default to false
    @Query(value = "SELECT vp from VideoPurchase vp where vp.userId = :userId and vp.isRefunded = false and vp.videoId in :videoId and vp.duration = 'PERMANENT'")
    List<VideoPurchase> getPermanentVideoPurchasesByUserIdAndVideoId(@Param("userId") String userId, @Param("videoId") Set<String> videoId);

    List<VideoPurchase> findByPackagePurchaseId(String packagePurchaseId);
}
