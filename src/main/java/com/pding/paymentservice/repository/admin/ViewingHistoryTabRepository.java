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
            "WHERE vp.user_id = ?1 order by vp.last_update_date desc",
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

    @Query(value = "SELECT COUNT(DISTINCT v.video_id) AS videoCount, SUM(vw.count) AS totalViews\n" +
            " FROM videos v INNER JOIN video_view_count vw ON v.video_id = vw.video_id\n" +
            " WHERE v.user_id = :pdUserId", nativeQuery = true)
    Object[] getVideoSummaryByPdUserId(@Param("pdUserId") String userId);

    @Query(value = "  SELECT COUNT(DISTINCT vp.id) AS totalVideoSales, COALESCE (SUM(trees_consumed), 0) AS totalProfit\n" +
            "  FROM video_purchase vp INNER JOIN videos v ON v.user_id = vp.video_owner_user_id AND v.video_id = vp.video_id\n" +
            " WHERE vp.video_owner_user_id= :pdUserId", nativeQuery = true)
    Object[] getVideoSalesTotalsByPdUserId(@Param("pdUserId") String userId);

    @Query(value = "SELECT video_id, title, views, GROUP_CONCAT(salePrice SEPARATOR ', ') AS salePrices, SUM(profit), MAX(last_update_date) \n" +
            "FROM ( SELECT COALESCE(v.video_id, '') AS video_id, \n" +
            "\tCOALESCE(v.title, '') AS title, \n" +
            "\tCOALESCE(SUM(vw.count), '0.0') AS views, \n" +
            "\tCOALESCE(CONCAT(COUNT(distinct vp.id), '/', trees_consumed), '0/0.0') AS salePrice, \n" +
            "\tCOALESCE(SUM(vp.trees_consumed), '0.0') AS profit, \n" +
            "\tCOALESCE(MAX(vp.last_update_date), '') AS last_update_date \n" +
            "\tFROM videos v LEFT JOIN video_view_count vw ON vw.video_id = v.video_id \n" +
            "\tLEFT JOIN video_purchase vp ON vp.video_id = v.video_id AND v.user_id = vp.video_owner_user_id \n" +
            "\tWHERE v.user_id = :pdUserId \n" +
            "\t\t AND ((:searchString IS NULL) OR (v.title LIKE %:searchString%)) \n" +
            "\tGROUP BY v.video_id, v.title, trees_consumed, vw.count ) AS subquery \n" +
            " GROUP BY  title, video_id, views ",
            countQuery = "SELECT COUNT(*) FROM videos v " +
                    "WHERE v.user_id = :pdUserId " +
                    "AND ((:searchString IS NULL) OR (v.title LIKE %:searchString%)) ",
            nativeQuery = true)
    Page<Object[]> getVideoSalesHistoryByPdIdAndVideoTitle(String pdUserId, String searchString, Pageable pageable);



}

