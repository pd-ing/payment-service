package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, String> {
    List<Donation> findByDonorUserId(String donorUserId);

    @Query(value = "SELECT u.email AS donor_email, d.donated_trees, d.last_update_date " +
            "FROM donation d " +
            "JOIN users u ON d.donor_user_id = u.id " +
            "WHERE d.pd_user_id = ?1 ORDER BY d.last_update_date ASC", countQuery = "SELECT count(d.id) FROM Donation d WHERE d.pd_user_id = ?1", nativeQuery = true)
    Page<Object[]> findByPdUserId(String pdUserId, Pageable pageable);

    @Query(value = "SELECT u.email AS donor_email, d.donated_trees, d.last_update_date, " +
            "(SELECT COUNT(vp.id) FROM video_purchase vp WHERE vp.user_id = d.donor_user_id AND vp.video_owner_user_id = d.pd_user_id) AS total_videos_purchased, " +
            "(SELECT d2.donated_trees FROM donation d2 WHERE d2.donor_user_id = d.donor_user_id AND d2.pd_user_id = d.pd_user_id ORDER BY d2.last_update_date DESC LIMIT 1) AS recent_donation, " +
            "u.id as userId " +
            "FROM donation d " +
            "JOIN users u ON d.donor_user_id = u.id " +
            "WHERE d.pd_user_id = ?1 " +
            "ORDER BY d.last_update_date ASC",
            countQuery = "SELECT COUNT(d.id) FROM donation d WHERE d.pd_user_id = ?1",
            nativeQuery = true)
    Page<Object[]> findDonationHistoryWithVideoStatsByPdUserId(String pdUserId, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM videos v WHERE v.user_id = ?1", nativeQuery = true)
    Long countTotalVideosUploadedByPdUserId(String pdUserId);

    @Query("SELECT d.donorUserId, SUM(d.donatedTrees) as totalDonatedTrees " +
            "FROM Donation d " +
            "GROUP BY d.donorUserId " +
            "ORDER BY totalDonatedTrees DESC")
    List<Object[]> findTopDonors(@Param("limit") Long limit);

    default List<Object[]> findTopDonorUserIds(Long limit) {
        return findTopDonors(limit);

//        return topDonors.stream()
//                .map(row -> (String) row[0])
//                .collect(Collectors.toList());
    }


//    @Query("SELECT d.donorUserId, SUM(d.donatedTrees) as totalDonatedTrees " +
//            "FROM Donation d " +
//            "WHERE d.pdUserId = :pdUserId " +
//            "GROUP BY d.donorUserId " +
//            "ORDER BY totalDonatedTrees DESC " +
//            "LIMIT :limit")

    @Query(
        value = " select userID, sum(tree_amount) as total_tree" +
        " from ((SELECT d.donor_user_id userID, d.donated_trees as tree_amount" +
        "        FROM donation d" +
        "        WHERE d.pd_user_id = :pdUserId)" +
        "       union all" +
        "       (select vp.user_id as userID, vp.trees_consumed as tree_amount" +
        "        from video_purchase vp" +
        "        where vp.video_owner_user_id = :pdUserId" +
        "          and vp.is_refunded is not true)) as total_tree_usage" +
        " group by userID" +
        " limit :limit", nativeQuery = true)
    List<Object[]> findTopDonorUserAndDonatedTreesByPdUserID(@Param("pdUserId") String pdUserId, @Param("limit") Long limit);

    @Query(value =
            " select userId," +
            "        sum(totalPurchasedVideoTree) as totalVideoPurchase," +
            "        max(lastPurchaseDate)," +
            "        sum(totalTreeDonation)       as totalDonation," +
            "        max(lastDonationDate)," +
            "        u.email," +
            "        COALESCE(u.profile_picture, '')," +
            "        COALESCE(u.nickname, '')," +
            "        u.is_creator" +
            " from (SELECT vp.user_id             AS userId," +
            "              SUM(vp.trees_consumed) AS totalPurchasedVideoTree," +
            "              MAX(last_update_date)  AS lastPurchaseDate," +
            "              0                      as totalTreeDonation," +
            "              null                   as lastDonationDate" +
            "       FROM video_purchase vp" +
            "       WHERE vp.video_owner_user_id = :pdUserId" +
            "         AND vp.is_refunded IS NOT TRUE" +
            "       GROUP BY vp.user_id" +
            "       union all" +
            "       SELECT donor_user_id         AS userId," +
            "              0                     as totalPurchasedVideoTree," +
            "              null                  as lastPurchaseDate," +
            "              SUM(donated_trees)    AS totalTreeDonation," +
            "              MAX(last_update_date) AS lastDonationDate" +
            "       FROM donation" +
            "       WHERE pd_user_id = :pdUserId" +
            "       GROUP BY donor_user_id) as temp" +
            "          left join users u on u.id = temp.userId" +
            " where (:searchString is null or :searchString = '' or u.email like concat('%', :searchString, '%') or" +
            "        u.nickname like concat('%', :searchString, '%'))" +
            " group by userId" +
            " order by totalVideoPurchase + totalDonation desc",
        countQuery =
                    " select count(*)" +
                    " from (select *" +
                    "       from (SELECT vp.user_id             AS userId," +
                    "                    SUM(vp.trees_consumed) AS totalPurchasedVideoTree," +
                    "                    MAX(last_update_date)  AS lastPurchaseDate," +
                    "                    0                      as totalTreeDonation," +
                    "                    null                   as lastDonationDate" +
                    "             FROM video_purchase vp" +
                    "             WHERE vp.video_owner_user_id = :pdUserId" +
                    "               AND vp.is_refunded IS NOT TRUE" +
                    "             GROUP BY vp.user_id" +
                    "             union all" +
                    "             SELECT donor_user_id         AS userId," +
                    "                    0                     as totalPurchasedVideoTree," +
                    "                    null                  as lastPurchaseDate," +
                    "                    SUM(donated_trees)    AS totalTreeDonation," +
                    "                    MAX(last_update_date) AS lastDonationDate" +
                    "             FROM donation" +
                    "             WHERE pd_user_id = :pdUserId" +
                    "             GROUP BY donor_user_id) as temp" +
                    "                left join users u on u.id = temp.userId" +
                    "       where (:searchString is null or :searchString = '' or u.email like concat('%', :searchString, '%') or" +
                    "              u.nickname like concat('%', :searchString, '%'))" +
                    "       group by userId) as count",
        nativeQuery = true)
    Page<Object[]> findTopDonorUser(@Param("pdUserId") String pdUserId, @Param("searchString") String searchString, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(d.donatedTrees), 0) FROM Donation d WHERE d.donorUserId = :userId")
    BigDecimal getTotalDonatedTreesByDonorUserId(@Param("userId") String userId);

    @Query(value = "select d from Donation d where d.pdUserId = :pdId and d.lastUpdateDate >= :startDate and d.lastUpdateDate <= :endDate")
    List<Donation> findDonationsByPdIdAndDateRange(@Param("pdId") String pdId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "" +
            " select d.donor_user_id, " +
            "       sum(d.donated_trees)                 as totalTreeDonation, " +
            "       (select sum(trees_consumed) " +
            "        from video_purchase vp " +
            "        where vp.video_owner_user_id = :pdUserId " +
            "          and vp.user_id = d.donor_user_id " +
            "          and vp.last_update_date >= :startDate " +
            "          and vp.last_update_date <= :endDate) as totalPurchasedVideoTree, " +
            "       (select max(last_update_date) " +
            "        from video_purchase vp " +
            "        where vp.video_owner_user_id = :pdUserId " +
            "          and vp.user_id = d.donor_user_id " +
            "          and vp.last_update_date >= :startDate " +
            "          and vp.last_update_date <= :endDate) as lastPurchasedVideoDate, " +
            "       max(d.last_update_date)              as lastDonationDate " +
            " from donation d " +
            " where d.pd_user_id = :pdUserId " +
            "   and d.last_update_date >= :startDate " +
            "   and d.last_update_date <= :endDate " +
            " group by d.donor_user_id " +
            " having sum(d.donated_trees) > 0 " +
            "     or (select sum(trees_consumed) " +
            "         from video_purchase vp " +
            "         where vp.video_owner_user_id = :pdUserId " +
            "           and vp.user_id = d.donor_user_id " +
            "           and vp.last_update_date >= :startDate " +
            "           and vp.last_update_date <= :endDate) > 0 " +
            " ORDER BY totalTreeDonation + totalPurchasedVideoTree desc",
            countQuery = "select count(*) from donation d " +
                    " where d.pd_user_id = :pdUserId " +
                    "   and d.last_update_date >= :startDate " +
                    "   and d.last_update_date <= :endDate " +
                    " group by d.donor_user_id " +
                    " having sum(d.donated_trees) > 0 " +
                    "     or (select sum(trees_consumed) " +
                    "         from video_purchase vp " +
                    "         where vp.video_owner_user_id = :pdUserId " +
                    "           and vp.user_id = d.donor_user_id " +
                    "           and vp.last_update_date >= :startDate " +
                    "           and vp.last_update_date <= :endDate) > 0",
            nativeQuery = true)
    List<Object[]> findTopDonorUserByDateRanger(String pdUserId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT d.donorUserId FROM Donation d WHERE d.lastUpdateDate >= :fromDateTime")
    List<String> findUsersPurchaseFromLastUpdate(@Param("fromDateTime") LocalDateTime fromDateTime);
}
