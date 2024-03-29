package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.VideoPurchaseHistoryForAdminDashboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ViewingHistoryTabRepository extends JpaRepository<VideoPurchase, String> {

    @Query(value = "SELECT COUNT(*) FROM video_purchase " +
            "WHERE user_id = :userId " +
            "AND YEAR(last_update_date) = YEAR(CURRENT_DATE) " +
            "AND MONTH(last_update_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
    BigDecimal totalVideosPurchasedByUserInCurrentMonth(@Param("userId") String userId);


    @Query(value = "SELECT COALESCE(SUM(trees_consumed), 0) FROM video_purchase " +
            "WHERE user_id = :userId " +
            "AND YEAR(last_update_date) = YEAR(CURRENT_DATE) " +
            "AND MONTH(last_update_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
    BigDecimal totalTreesConsumedByUserInCurrentMonth(@Param("userId") String userId);

//    @Query(value = "SELECT NEW com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.VideoPurchaseHistoryForAdminDashboard(" +
//            "vp.last_update_date," +
//            "vp.video_id, " +
//            "vt.path, " +
//            "v.title, " +
//            "u.profile_id, " +
//            "vp.trees_consumed) " +
//            "FROM video_purchase vp " +
//            "LEFT JOIN video_thumbnail vt ON vp.video_id = vt.video_id " +
//            "LEFT JOIN videos v ON vp.video_id = v.id " +
//            "LEFT JOIN users u ON v.user_id = u.id " +
//            "WHERE vp.user_id = ?1", nativeQuery = true)
//    List<VideoPurchaseHistoryForAdminDashboard> findVideoPurchaseHistoryByUserId(String userId);

//    @Query(value = "SELECT COALESCE(vp.last_update_date, ''), " +
//            "COALESCE(vp.video_id, ''), " +
//            "COALESCE(vt.path, ''), " +
//            "COALESCE(v.title, ''), " +
//            "COALESCE(u.profile_id, ''), " +
//            "COALESCE(vp.trees_consumed, '') " +
//            "FROM video_purchase vp " +
//            "LEFT JOIN video_thumbnail vt ON vp.video_id = vt.video_id " +
//            "LEFT JOIN videos v ON vp.video_id = v.video_id " +
//            "LEFT JOIN users u ON v.user_id = u.id " +
//            "WHERE vp.user_id = ?1", nativeQuery = true)
//    List<Object[]> findVideoPurchaseHistoryByUserId(String userId);

    @Query(value = "SELECT COALESCE(vp.last_update_date, ''), " +
            "COALESCE(vp.video_id, ''), " +
            "COALESCE(vt.path, ''), " +
            "COALESCE(v.title, ''), " +
            "COALESCE(u.profile_id, ''), " +
            "COALESCE(vp.trees_consumed, '') " +
            "FROM video_purchase vp " +
            "LEFT JOIN video_thumbnail vt ON vp.video_id = vt.video_id " +
            "LEFT JOIN videos v ON vp.video_id = v.video_id " +
            "LEFT JOIN users u ON v.user_id = u.id " +
            "WHERE vp.user_id = ?1",
            countQuery = "SELECT COUNT(*) FROM video_purchase vp WHERE vp.user_id = ?1",
            nativeQuery = true)
    Page<Object[]> findVideoPurchaseHistoryByUserId(String userId, Pageable pageable);

    @Query(value = "SELECT COALESCE(vp.last_update_date, ''), " +
            "COALESCE(vp.video_id, ''), " +
            "COALESCE(vt.path, ''), " +
            "COALESCE(v.title, ''), " +
            "COALESCE(u.profile_id, ''), " +
            "COALESCE(vp.trees_consumed, '') " +
            "FROM video_purchase vp " +
            "LEFT JOIN video_thumbnail vt ON vp.video_id = vt.video_id " +
            "LEFT JOIN videos v ON vp.video_id = v.video_id " +
            "LEFT JOIN users u ON v.user_id = u.id " +
            "WHERE vp.user_id = ?1 " +
            "AND v.title LIKE %?2%",
            countQuery = "SELECT COUNT(*) FROM video_purchase vp LEFT JOIN videos v ON vp.video_id = v.video_id WHERE vp.user_id = ?1 AND v.title LIKE %?2%",
            nativeQuery = true)
    Page<Object[]> findVideoPurchaseHistoryByUserIdAndVideoTitle(String userId, String videoTitle, Pageable pageable);
}

