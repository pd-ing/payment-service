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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
