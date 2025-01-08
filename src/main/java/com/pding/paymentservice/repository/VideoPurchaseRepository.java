package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.tables.inner.VideoEarningsAndSales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
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

    //VideoPurchase save(VideoPurchase transaction);

    List<VideoPurchase> getVideoPurchaseByUserId(String userId);

    @Query("SELECT SUM(vt.treesConsumed) FROM VideoPurchase vt WHERE vt.videoOwnerUserId = :videoOwnerUserId and vt.isRefunded != true")
    BigDecimal getTotalTreesEarnedByVideoOwner(String videoOwnerUserId);

    @Query("SELECT vp from VideoPurchase vp where vp.userId = :userId and vp.videoId = :videoId and vp.isRefunded != true")
    List<VideoPurchase> findByUserIdAndVideoId(String userId, String videoId);

    @Query("SELECT vp.videoId, COALESCE(SUM(vp.treesConsumed), 0), COUNT(vp) FROM VideoPurchase vp WHERE vp.videoId IN :videoIds and vp.isRefunded != true GROUP BY vp.videoId")
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

    @Query("SELECT vp from VideoPurchase vp where vp.videoId = :videoId and vp.isRefunded != true order by vp.lastUpdateDate desc")
    Page<VideoPurchase> findAllByVideoIdOrderByLastUpdateDateDesc(String videoId, Pageable pageable);

    @Query("SELECT vp from VideoPurchase vp where vp.videoId = :videoId and vp.userId in :onlyTheseUsersList and vp.isRefunded != true order by vp.lastUpdateDate desc")
    Page<VideoPurchase> findAllByVideoIdAndUserIdInOrderByLastUpdateDateDesc(String videoId, List<String> onlyTheseUsersList, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(vp.treesConsumed), 0) FROM VideoPurchase vp WHERE vp.userId = :userId and vp.isRefunded != true")
    BigDecimal getTotalTreesConsumedByUserId(@Param("userId") String userId);

    @Query(value =
            "SELECT COALESCE(vp.last_update_date, ''), " +
            "COALESCE(v.title, ''), " +
            "COALESCE(vp.trees_consumed, ''), " +
            "COALESCE(u.email, ''), " +
            "COALESCE(vp.duration, ''), " +
            "COALESCE(vp.expiry_date, '') " +
            "FROM video_purchase vp " +
            "LEFT JOIN videos v ON vp.video_id = v.video_id " +
            "LEFT JOIN users u ON vp.user_id = u.id " +
            "WHERE vp.video_owner_user_id = :userId and vp.is_refunded != true" +
            "AND (:searchString IS NULL OR u.email like concat('%', :searchString, '%') OR v.title like concat('%', :searchString, '%')) " +
            "AND (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR vp.last_update_date < :endDate) ",
            nativeQuery = true)
    Page<Object[]> getSalesHistoryByUserIdAndDates(String searchString, String userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query(value =
            "SELECT COALESCE(vp.last_update_date, ''), " +
                    "COALESCE(vp.trees_consumed, ''), " +
                    "COALESCE(u.email, ''), " +
                    "COALESCE(vp.duration, ''), " +
                    "COALESCE(vp.expiry_date, '') " +
                    "FROM video_purchase vp " +
                    "LEFT JOIN videos v ON vp.video_id = v.video_id " +
                    "LEFT JOIN users u ON vp.user_id = u.id " +
                    "WHERE vp.video_owner_user_id = :userId and vp.is_refunded != true" +
                    "AND (:searchString IS NULL OR u.email like concat('%', :searchString, '%') OR v.title like concat('%', :searchString, '%')) " +
                    "AND (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
                    "AND (:endDate IS NULL OR vp.last_update_date < :endDate)"+
                    "ORDER BY CASE WHEN :sortDirection = 'ASC' THEN vp.last_update_date END ASC, " +
                    "CASE WHEN :sortDirection = 'DESC' THEN vp.last_update_date END DESC",
            nativeQuery = true)
    List<Object[]> getAllSalesHistoryByUserIdAndDates(String searchString, String userId, LocalDate startDate, LocalDate endDate, String sortDirection);

    @Query(value =
            "SELECT sum(vp.trees_consumed) " +
            "FROM video_purchase vp " +
            "WHERE vp.video_owner_user_id = :userId and vp.is_refunded != true" +
            "AND (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR vp.last_update_date < :endDate) ",
            nativeQuery = true)
   Long getTotalTreesEarned(String userId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT DISTINCT uf.follower, vp.user_id \n" +
            "FROM user_followings uf LEFT join video_purchase vp \n" +
            "ON uf.follower = vp.user_id\n" +
            "WHERE uf.is_deleted = FALSE and uf.following = :userId and vp.is_refunded != true",
            nativeQuery = true)
    List<Object[]> getFollowersList(String userId);

    @Query(value = "SELECT COALESCE(SUM(vt.trees_consumed), 0) FROM video_purchase vt WHERE vt.video_owner_user_id = ?1 AND vt.last_update_date >= DATE_SUB(?2, INTERVAL 24 HOUR) and vp.is_refunded != true", nativeQuery = true)
    BigDecimal getDailyTreeRevenueByVideoOwner(String videoOwnerUserId, LocalDateTime endDateTime);

    @Query("SELECT DISTINCT vp.videoOwnerUserId FROM VideoPurchase vp WHERE vp.userId = ?1 and vp.isRefunded != true")
    Page<String> getAllPdUserIdWhoseVideosArePurchasedByUser(String userId, Pageable pageable);

    @Query("SELECT vp from VideoPurchase vp where vp.userId = :userId and vp.videoId in :videoIds and vp.isRefunded != true")
    List<VideoPurchase> findByUserIdAndVideoIdIn(String userId, Set<String> videoIds);

    @Query("SELECT vp from VideoPurchase vp where vp.userId = :userId and vp.isRefunded != true")
    List<VideoPurchase> findByUserId(String userId);

    @Query(value = "select vp from VideoPurchase vp where vp.expiryDate > current_date and vp.userId = :userId and (:ownerId is null or vp.videoOwnerUserId = :ownerId) and vp.isRefunded != true")
    Page<VideoPurchase> findNotExpiredVideo(@Param("userId") String userId, @Param("ownerId") String ownerId, Pageable pageable);


    @Query(value =
            " select *, max(vp.expiry_date) as maxExpiryDate" +
            " from video_purchase vp" +
            " where 1 = 1 and vp.is_refunded != true" +
            "   and user_id = :userId" +
            "   and (:ownerId is null" +
            "     or video_owner_user_id = :ownerId)" +
            " group by video_id, user_id, video_owner_user_id" +
            " having maxExpiryDate < now()", nativeQuery = true)
    Page<VideoPurchase> findExpiredVideoPurchases(@Param("userId") String userId, @Param("ownerId") String ownerId, Pageable pageable);

    @Query("select count(vp) from VideoPurchase vp where vp.videoId = :videoId and vp.isRefunded != true")
    Long countByVideoId(String videoId);

    @Query(value = "select count(distinct user_id)" +
            " from video_purchase" +
            " where video_id = :videoId and is_refunded != true", nativeQuery = true)
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
                   " where vp.video_id = :videoId and vp.is_refunded != true" +
                   " group by vp.video_id, vp.user_id", nativeQuery = true)
    Page<Object[]> getSaleHistoryByVideoId(@Param("videoId") String videoId, Pageable pageable);


    @Query(value = "SELECT vp from VideoPurchase vp where vp.videoOwnerUserId = :videoOwnerUserId and vp.isRefunded != true and vp.lastUpdateDate >= :startDate and vp.lastUpdateDate <= :endDate")
    List<VideoPurchase> getVideoPurchasesByVideoOwnerUserIdAndDates(String videoOwnerUserId, LocalDateTime startDate, LocalDateTime endDate);
}
