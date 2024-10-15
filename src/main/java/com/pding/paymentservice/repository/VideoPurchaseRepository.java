package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.tables.inner.VideoEarningsAndSales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<VideoPurchase> findByUserId(String userId, Pageable pageable);

    Page<VideoPurchase> findByUserIdAndVideoOwnerUserId(String userId, String videoOwnerUserId, Pageable pageable);


    @Query("SELECT SUM(vt.treesConsumed) FROM VideoPurchase vt WHERE vt.videoOwnerUserId = :videoOwnerUserId")
    BigDecimal getTotalTreesEarnedByVideoOwner(String videoOwnerUserId);

    // Query method to find records by userID and videoID
    List<VideoPurchase> findByUserIdAndVideoId(String userId, String videoId);

//    @Query("SELECT vp.videoId, COALESCE(SUM(vp.treesConsumed), 0), COUNT(vp) FROM VideoPurchase vp WHERE vp.videoId IN :videoIds GROUP BY vp.videoId")
//    List<Object[]> getTotalTreesEarnedAndSalesCountForVideoIds(@Param("videoIds") List<String> videoIds);
//
//    default Map<String, VideoEarningsAndSales> getTotalTreesEarnedAndSalesCountMapForVideoIds(List<String> videoIds) {
//        List<Object[]> results = getTotalTreesEarnedAndSalesCountForVideoIds(videoIds);
//        return results.stream()
//                .collect(Collectors.toMap(
//                        result -> (String) result[0],  // videoId
//                        result -> new VideoEarningsAndSales(
//                                (BigDecimal) result[1],  // total trees earned
//                                (Long) result[2]  // total sales count
//                        )
//                ));
//    }

    @Query("SELECT vp.videoId, COALESCE(SUM(vp.treesConsumed), 0), COUNT(vp) FROM VideoPurchase vp WHERE vp.videoId IN :videoIds GROUP BY vp.videoId")
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

    @Query(value = "SELECT trees FROM videos WHERE video_id = :videoId AND user_id = :userId", nativeQuery = true)
    BigDecimal findActualCostOfVideo(@Param("videoId") String videoId, @Param("userId") String userId);

    @Query(value = "SELECT user_id, trees FROM videos WHERE video_id = :videoId", nativeQuery = true)
    List<Object[]> findUserIdAndTreesByVideoId(@Param("videoId") String videoId);

    Page<VideoPurchase> findAllByVideoIdOrderByLastUpdateDateDesc(String videoId, Pageable pageable);

    Page<VideoPurchase> findAllByVideoIdAndUserIdInOrderByLastUpdateDateDesc(String videoId, List<String> onlyTheseUsersList, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(vp.treesConsumed), 0) FROM VideoPurchase vp WHERE vp.userId = :userId")
    BigDecimal getTotalTreesConsumedByUserId(@Param("userId") String userId);

    @Query(value =
            "SELECT COALESCE(vp.last_update_date, ''), " +
            "COALESCE(v.title, ''), " +
            "COALESCE(v.trees, ''), " +
            "COALESCE(u.email, ''), " +
            "COALESCE(vp.duration, ''), " +
            "COALESCE(vp.expiry_date, '') " +
            "FROM video_purchase vp " +
            "LEFT JOIN videos v ON vp.video_id = v.video_id " +
            "LEFT JOIN users u ON vp.user_id = u.id " +
            "WHERE vp.video_owner_user_id = :userId " +
            "AND (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR vp.last_update_date <= :endDate) ",
            countQuery = "SELECT COUNT(*) FROM video_purchase vp WHERE vp.video_owner_user_id = :userId AND (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
                    "AND (:endDate IS NULL OR vp.last_update_date <= :endDate)",
            nativeQuery = true)
    Page<Object[]> getSalesHistoryByUserIdAndDates(String userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query(value = "SELECT DISTINCT uf.follower, vp.user_id \n" +
            "FROM user_followings uf LEFT join video_purchase vp \n" +
            "ON uf.follower = vp.user_id\n" +
            "WHERE uf.is_deleted = FALSE and uf.following = :userId",
            nativeQuery = true)
    List<Object[]> getFollowersList(String userId);

    @Query(value = "SELECT COALESCE(vp.last_update_date, ''), " +
            "COALESCE(v.title, ''), " +
            "COALESCE(v.trees, ''), " +
            "COALESCE(u.email, ''), " +
            "COALESCE(vp.duration, ''), " +
            "COALESCE(vp.expiry_date '') " +
            "FROM video_purchase vp " +
            "LEFT JOIN videos v ON vp.video_id = v.video_id " +
            "LEFT JOIN users u ON vp.user_id = u.id " +
            "WHERE vp.video_owner_user_id = :userId " +
            "AND u.email LIKE %:searchString%",
            countQuery = "SELECT COUNT(*) FROM video_purchase vp WHERE vp.video_owner_user_id = :userId AND " +
                    "u.email LIKE %:searchString%",
            nativeQuery = true)
    Page<Object[]> searchSalesHistoryByUserId(String userId, String searchString, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(vt.trees_consumed), 0) FROM video_purchase vt WHERE vt.video_owner_user_id = ?1 AND vt.last_update_date >= DATE_SUB(?2, INTERVAL 24 HOUR)", nativeQuery = true)
    BigDecimal getDailyTreeRevenueByVideoOwner(String videoOwnerUserId, LocalDateTime endDateTime);

    @Query("SELECT DISTINCT vp.videoOwnerUserId FROM VideoPurchase vp WHERE vp.userId = ?1")
    Page<String> getAllPdUserIdWhoseVideosArePurchasedByUser(String userId, Pageable pageable);

    List<VideoPurchase> findByUserIdAndVideoIdIn(String userId, Set<String> videoIds);
    List<VideoPurchase> findByUserId(String userId);

    @Query(value =
            " select *" +
            " from video_purchase" +
            " where ( duration = 'PERMANENT'" +
            "    or expiry_date > NOW()) and user_id = :userId and (:ownerId is null or video_owner_user_id = :ownerId)", nativeQuery = true)
    Page<VideoPurchase> findNotExpiredVideo(@Param("userId") String userId, @Param("ownerId") String ownerId, Pageable pageable);


    @Query(value =
            " select *, max(vp.expiry_date) as maxExpiryDate" +
            " from video_purchase vp" +
            " where duration != 'PERMANENT'" +
            "   and user_id = :userId" +
            "   and (:ownerId is null" +
            "     or video_owner_user_id = :ownerId)" +
            " group by video_id, user_id, video_owner_user_id" +
            " having maxExpiryDate < now()", nativeQuery = true)
    Page<VideoPurchase> findExpiredVideoPurchases(@Param("userId") String userId, @Param("ownerId") String ownerId, Pageable pageable);
}
