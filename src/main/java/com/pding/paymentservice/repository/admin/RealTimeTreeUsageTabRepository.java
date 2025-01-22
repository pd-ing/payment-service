package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface RealTimeTreeUsageTabRepository extends JpaRepository<VideoPurchase, String> {

    //    @Query(value = "(" +
//            " SELECT COALESCE(u.email, ''), COALESCE(vp.last_update_date, '') AS last_update_date,  COALESCE(vp.trees_consumed, 0), " +
//            " 'VIDEO', COALESCE(pd.nickname, ''), COALESCE(pd.id, '') " +
//            " FROM video_purchase vp " +
//            " LEFT JOIN users u ON vp.user_id = u.id " +
//            " LEFT JOIN users pd ON vp.video_owner_user_id = pd.id " +
//            " WHERE (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
//            " AND (:endDate IS NULL OR  vp.last_update_date <= :endDate) " +
//            " AND ((:searchString IS NULL) OR (u.email LIKE %:searchString% OR pd.nickname LIKE %:searchString%)) " +
//            " ) " +
//            " UNION ALL " +
//            " (" +
//            " SELECT COALESCE(u.email, ''), COALESCE(d.last_update_date, '')  AS last_update_date,  COALESCE(d.donated_trees, 0), " +
//            " 'DONATION', COALESCE(pd_don.nickname, ''), COALESCE(pd_don.id, '') " +
//            " FROM donation d " +
//            " LEFT JOIN users u ON d.donor_user_id = u.id " +
//            " LEFT JOIN users pd_don ON d.pd_user_id = pd_don.id " +
//            " WHERE (:startDate IS NULL OR d.last_update_date >= :startDate) " +
//            " AND (:endDate IS NULL OR  d.last_update_date <= :endDate) " +
//            " AND ((:searchString IS NULL) OR (u.email LIKE %:searchString% OR pd_don.nickname LIKE %:searchString%)) " +
//            " )",
//            countQuery = "SELECT COUNT(*) FROM \n" +
//                    "( \n" +
//                    "    (SELECT * FROM (\n" +
//                    "        SELECT COALESCE(u.email, '') AS email, \n" +
//                    "               COALESCE(vp.last_update_date, '') AS last_update_date,  \n" +
//                    "               COALESCE(vp.trees_consumed, 0) AS trees_consumed, \n" +
//                    "               'VIDEO' AS transaction_type, \n" +
//                    "               COALESCE(pd.nickname, '') AS nickname, \n" +
//                    "               COALESCE(pd.id, '') AS user_id \n" +
//                    "        FROM video_purchase vp \n" +
//                    "        LEFT JOIN users u ON vp.user_id = u.id \n" +
//                    "        LEFT JOIN users pd ON vp.video_owner_user_id = pd.id \n" +
//                    "        WHERE  (:startDate IS NULL OR vp.last_update_date >= :startDate) \n" +
//                    "         AND (:endDate IS NULL OR vp.last_update_date <= :endDate) \n" +
//                    "         AND ((:searchString IS NULL) OR (u.email LIKE %:searchString% OR pd.nickname LIKE %:searchString%)) \n" +
//                    "    ) AS t )\n" +
//                    "    \n" +
//                    "    UNION ALL \n" +
//                    "    \n" +
//                    "    (\n" +
//                    "    SELECT * FROM ( \n" +
//                    "        SELECT COALESCE(u.email, '') AS email, \n" +
//                    "               COALESCE(d.last_update_date, '') AS last_update_date,  \n" +
//                    "               COALESCE(d.donated_trees, 0) AS trees_consumed, \n" +
//                    "               'DONATION' AS transaction_type, \n" +
//                    "               COALESCE(pd_don.nickname, '') AS nickname, \n" +
//                    "               COALESCE(pd_don.id, '') AS user_id \n" +
//                    "        FROM donation d \n" +
//                    "        LEFT JOIN users u ON d.donor_user_id = u.id \n" +
//                    "        LEFT JOIN users pd_don ON d.pd_user_id = pd_don.id \n" +
//                    "        WHERE (:startDate IS NULL OR d.last_update_date >= :startDate) \n" +
//                    "         AND (:endDate IS NULL OR d.last_update_date <= :endDate) \n" +
//                    "         AND ((:searchString IS NULL) OR (u.email LIKE %:searchString% OR pd_don.nickname LIKE %:searchString%)) \n" +
//                    "    ) AS t2 )\n" +
//                    ") AS total_count",
//            nativeQuery = true)
    @Query(value =
        " (SELECT COALESCE(u.email, '')," +
        "         COALESCE(u.id, '')," +
        "         COALESCE(vp.last_update_date, '') AS last_update_date," +
        "         COALESCE(vp.trees_consumed, 0)," +
        "         'VIDEO' as transaction_type," +
        "         COALESCE(pd.nickname, '')," +
        "         COALESCE(pd.id, '')," +
        "         vp.id," +
        "         case is_refunded when true then 'REFUNDED' else 'COMPLETED' end as status" +
        "  FROM video_purchase vp" +
        "           LEFT JOIN users u ON vp.user_id = u.id" +
        "           LEFT JOIN users pd ON vp.video_owner_user_id = pd.id" +
        "  WHERE (:startDate IS NULL OR vp.last_update_date >= :startDate)" +
        "    AND (:endDate IS NULL OR vp.last_update_date <= :endDate)" +
        "    AND ((:searchString IS NULL) OR" +
        "         (u.email LIKE concat('%', :searchString, '%') OR pd.nickname LIKE concat('%', :searchString, '%'))))" +
        " UNION ALL" +
        " (SELECT COALESCE(u.email, '')," +
        "         COALESCE(u.id, '')," +
        "         COALESCE(d.last_update_date, '') AS last_update_date," +
        "         COALESCE(d.donated_trees, 0)," +
        "         'DONATION' as transaction_type," +
        "         COALESCE(pd_don.nickname, '')," +
        "         COALESCE(pd_don.id, '')," +
        "         d.id," +
        "         'COMPLETED' as status" +
        "  FROM donation d" +
        "           LEFT JOIN users u" +
        "                     ON d.donor_user_id = u.id" +
        "           LEFT JOIN users pd_don ON d.pd_user_id = pd_don.id" +
        "  WHERE (:startDate IS NULL" +
        "      OR d.last_update_date >= :startDate)" +
        "    AND (:endDate IS NULL" +
        "      OR d.last_update_date <= :endDate)" +
        "    AND ((:searchString IS NULL)" +
        "      OR (u.email LIKE concat('%', :searchString, '%')" +
        "          OR pd_don.nickname LIKE concat('%', :searchString, '%'))))" +
        " UNION ALL" +
        " (select COALESCE(u.email, '')," +
        "         COALESCE(u.id, '')," +
        "         COALESCE(ticket.purchased_date, '') AS last_update_date," +
        "         COALESCE(ticket.trees_consumed, 0)," +
        "         'EXPOSURE_TICKET' as transaction_type," +
        "         ''," +
        "         ''," +
        "         ticket.id," +
        "         ticket.status as status" +
        "  from exposure_ticket_purchase ticket" +
        "           left join users u on ticket.user_id = u.id" +
        "  where ticket.is_give_by_admin is not true and (:startDate IS NULL" +
        "      OR ticket.purchased_date >= :startDate)" +
        "    and (:endDate IS NULL" +
        "      OR ticket.purchased_date <= :endDate)" +
        "    AND ((:searchString IS NULL)" +
        "      OR (u.email LIKE concat('%', :searchString, '%')" +
        "          OR u.nickname LIKE concat('%', :searchString, '%'))))",
            countQuery = "" +
                " select count(*) " +
                " from ((SELECT COALESCE(u.email, ''), " +
                "              COALESCE(u.id, ''), " +
                "              COALESCE(vp.last_update_date, '') AS last_update_date, " +
                "              COALESCE(vp.trees_consumed, 0), " +
                "              'VIDEO'                           as transaction_type, " +
                "              COALESCE(pd.nickname, ''), " +
                "              COALESCE(pd.id, ''), " +
                "              vp.id " +
                "       FROM video_purchase vp " +
                "                LEFT JOIN users u ON vp.user_id = u.id " +
                "                LEFT JOIN users pd ON vp.video_owner_user_id = pd.id " +
                "       WHERE (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
                "         AND (:endDate IS NULL OR vp.last_update_date <= :endDate) " +
                "         AND ((:searchString IS NULL) OR " +
                "              (u.email LIKE concat('%', :searchString, '%') OR pd.nickname LIKE concat('%', :searchString, '%')))) " +
                "      UNION ALL " +
                "      (SELECT COALESCE(u.email, ''), " +
                "              COALESCE(u.id, ''), " +
                "              COALESCE(d.last_update_date, '') AS last_update_date, " +
                "              COALESCE(d.donated_trees, 0), " +
                "              'DONATION'                       as transaction_type, " +
                "              COALESCE(pd_don.nickname, ''), " +
                "              COALESCE(pd_don.id, ''), " +
                "              d.id " +
                "       FROM donation d " +
                "                LEFT JOIN users u " +
                "                          ON d.donor_user_id = u.id " +
                "                LEFT JOIN users pd_don ON d.pd_user_id = pd_don.id " +
                "       WHERE (:startDate IS NULL " +
                "           OR d.last_update_date >= :startDate) " +
                "         AND (:endDate IS NULL " +
                "           OR d.last_update_date <= :endDate) " +
                "         AND ((:searchString IS NULL) " +
                "           OR (u.email LIKE concat('%', :searchString, '%') " +
                "               OR pd_don.nickname LIKE concat('%', :searchString, '%')))) " +
                "      UNION ALL " +
                "      (select COALESCE(u.email, ''), " +
                "              COALESCE(u.id, ''), " +
                "              COALESCE(ticket.purchased_date, '') AS last_update_date, " +
                "              COALESCE(ticket.trees_consumed, 0), " +
                "              'EXPOSURE_TICKET'                   as transaction_type, " +
                "              '', " +
                "              '', " +
                "              ticket.id " +
                "       from exposure_ticket_purchase ticket " +
                "                left join users u on ticket.user_id = u.id " +
                "       where ticket.is_give_by_admin is not true and (:startDate IS NULL " +
                "           OR ticket.purchased_date >= :startDate) " +
                "         and (:endDate IS NULL " +
                "           OR ticket.purchased_date <= :endDate) " +
                "         AND ((:searchString IS NULL) " +
                "           OR (u.email LIKE concat('%', :searchString, '%') " +
                "               OR u.nickname LIKE concat('%', :searchString, '%'))))) as total_count",
            nativeQuery = true)
    Page<Object[]> getRealTimeTreeUsage(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("searchString") String searchString, Pageable pageable);


    @Query(value = "SELECT  COALESCE(SUM(trees_consumed), 0) " +
            "FROM video_purchase " +
            "WHERE (:startDate IS NULL OR last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR  last_update_date <= :endDate) ",
            nativeQuery = true)
    BigDecimal getTotalTreesTransactedForVideos(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT  COALESCE(SUM(donated_trees),0) " +
            "FROM donation d " +
            "WHERE (:startDate IS NULL OR last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR  last_update_date <= :endDate) ",
            nativeQuery = true)
    BigDecimal getTotalTreesDonated(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT  COALESCE(SUM(trees_consumed), 0) " +
            "FROM exposure_ticket_purchase " +
            "WHERE is_give_by_admin is not true and (:startDate IS NULL OR purchased_date >= :startDate) " +
            "AND (:endDate IS NULL OR  purchased_date <= :endDate) and status != 'REFUNDED'",
            nativeQuery = true)
    BigDecimal getTotalTreesTransactedForExposureTickets(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
