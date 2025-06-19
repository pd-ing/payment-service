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
        "  WHERE vp.package_id is null and (:startDate IS NULL OR vp.last_update_date >= :startDate)" +
        "    AND (:endDate IS NULL OR vp.last_update_date <= :endDate)" +
        "    AND ((:searchString IS NULL) OR" +
        "         (u.email LIKE concat(:searchString, '%') OR pd.nickname LIKE concat(:searchString, '%')))" +
        "    AND (:transactionType IS NULL OR :transactionType = 'VIDEO')" +
        " )" +
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
        "      OR (u.email LIKE concat(:searchString, '%')" +
        "          OR pd_don.nickname LIKE concat(:searchString, '%')))" +
        "    AND (:transactionType IS NULL OR :transactionType = 'DONATION')" +
//        "    order by d.last_update_date desc" +
        " )" +
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
        "      OR (u.email LIKE concat(:searchString, '%')" +
        "          OR u.nickname LIKE concat(:searchString, '%')))" +
        "    AND (:transactionType IS NULL OR :transactionType = 'EXPOSURE_TICKET')" +
        " )" +
        " union all" +
        " (select COALESCE(u.email, '')," +
        "         COALESCE(u.id, '')," +
        "         COALESCE(mp.last_update_date, '') AS last_update_date," +
        "         COALESCE(mp.trees_transacted, 0)," +
        "         'MESSAGE'                         as transaction_type," +
        "         pd.nickname," +
        "         mp.pd_userid," +
        "         mp.id," +
        "         'COMPLETED'                       as status" +
        "  from message_purchase mp" +
        "           left join users u on mp.user_id = u.id" +
        "           left join users pd on mp.pd_userid = pd.id" +
        "  where mp.trees_transacted > 0" +
        "    and (:startDate IS NULL" +
        "      OR mp.last_update_date >= :startDate)" +
        "    and (:endDate IS NULL" +
        "      OR mp.last_update_date <= :endDate)" +
        "    AND ((:searchString IS NULL)" +
        "      OR (u.email LIKE concat(:searchString, '%')" +
        "          OR u.nickname LIKE concat(:searchString, '%')))" +
        "    AND (:transactionType IS NULL OR :transactionType = 'MESSAGE')" +
        "    )" +
        " union all" +
        " (select COALESCE(u.email, '')," +
        "         COALESCE(u.id, '')," +
        "         COALESCE(vpp.purchase_date, '') AS last_update_date," +
        "         COALESCE(vpp.trees_consumed, 0)," +
        "         COALESCE(vp.package_type, 'THEME_PACKAGE')  as transaction_type," +
        "         COALESCE(pd.nickname, '')," +
        "         COALESCE(pd.id, '')," +
        "         vpp.id," +
        "         case vpp.is_refunded when true then 'REFUNDED' else 'COMPLETED' end as status" +
        "  from video_package_purchase vpp" +
        "           left join users u on vpp.user_id = u.id" +
        "           left join users pd on vpp.seller_id = pd.id" +
        "           left join video_packages vp on vpp.package_id = vp.id" +
        "  where (:startDate IS NULL" +
        "      OR vpp.purchase_date >= :startDate)" +
        "    and (:endDate IS NULL" +
        "      OR vpp.purchase_date <= :endDate)" +
        "    AND ((:searchString IS NULL)" +
        "      OR (u.email LIKE concat(:searchString, '%')" +
        "          OR pd.nickname LIKE concat(:searchString, '%')))" +
        "    AND (:transactionType IS NULL OR :transactionType = COALESCE(vp.package_type, 'THEME_PACKAGE'))" +
        "    )"
        ,countQuery =
                " select count(*) " +
                " from ((SELECT vp.id " +
                "       FROM video_purchase vp " +
                "                LEFT JOIN users u ON vp.user_id = u.id " +
                "                LEFT JOIN users pd ON vp.video_owner_user_id = pd.id " +
                "       WHERE (:startDate IS NULL OR vp.last_update_date >= :startDate) " +
                "         AND (:endDate IS NULL OR vp.last_update_date <= :endDate) " +
                "         AND ((:searchString IS NULL) OR " +
                "              (u.email LIKE concat(:searchString, '%') OR pd.nickname LIKE concat(:searchString, '%')))" +
                "         AND (:transactionType IS NULL OR :transactionType = 'VIDEO')" +
                " ) " +
                "      UNION ALL " +
                "      (SELECT d.id " +
                "       FROM donation d " +
                "                LEFT JOIN users u " +
                "                          ON d.donor_user_id = u.id " +
                "                LEFT JOIN users pd_don ON d.pd_user_id = pd_don.id " +
                "       WHERE (:startDate IS NULL " +
                "           OR d.last_update_date >= :startDate) " +
                "         AND (:endDate IS NULL " +
                "           OR d.last_update_date <= :endDate) " +
                "         AND ((:searchString IS NULL) " +
                "           OR (u.email LIKE concat(:searchString, '%') " +
                "               OR pd_don.nickname LIKE concat(:searchString, '%')))" +
                "         AND (:transactionType IS NULL OR :transactionType = 'DONATION')" +
                ") " +
                "      UNION ALL " +
                "      (select ticket.id " +
                "       from exposure_ticket_purchase ticket " +
                "                left join users u on ticket.user_id = u.id " +
                "       where ticket.is_give_by_admin is not true and (:startDate IS NULL " +
                "           OR ticket.purchased_date >= :startDate) " +
                "         and (:endDate IS NULL " +
                "           OR ticket.purchased_date <= :endDate) " +
                "         AND ((:searchString IS NULL) " +
                "           OR (u.email LIKE concat(:searchString, '%') " +
                "               OR u.nickname LIKE concat(:searchString, '%')))" +
                "         AND (:transactionType IS NULL OR :transactionType = 'EXPOSURE_TICKET')" +
                ")" +
                " union all" +
                " (select mp.id " +
                "  from message_purchase mp" +
                "           left join users u on mp.user_id = u.id" +
                "           left join users pd on mp.pd_userid = pd.id" +
                "  where mp.trees_transacted > 0" +
                "    and (:startDate IS NULL" +
                "      OR mp.last_update_date >= :startDate)" +
                "    and (:endDate IS NULL" +
                "      OR mp.last_update_date <= :endDate)" +
                "    AND ((:searchString IS NULL)" +
                "      OR (u.email LIKE concat(:searchString, '%')" +
                "          OR u.nickname LIKE concat( :searchString, '%')))" +
                "    AND (:transactionType IS NULL OR :transactionType = 'MESSAGE'))" +
                " union all" +
                " (select vpp.id " +
                "  from video_package_purchase vpp" +
                "           left join users u on vpp.user_id = u.id" +
                "           left join users pd on vpp.seller_id = pd.id" +
                "           left join video_packages vp on vpp.package_id = vp.id" +
                "  where (:startDate IS NULL" +
                "      OR vpp.purchase_date >= :startDate)" +
                "    and (:endDate IS NULL" +
                "      OR vpp.purchase_date <= :endDate)" +
                "    AND ((:searchString IS NULL)" +
                "      OR (u.email LIKE concat(:searchString, '%')" +
                "          OR pd.nickname LIKE concat(:searchString, '%')))" +
                "    AND (:transactionType IS NULL OR :transactionType = COALESCE(vp.package_type, 'THEME_PACKAGE')))" +
                ") as total_count",
            nativeQuery = true)
    Page<Object[]> getRealTimeTreeUsage(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("transactionType") String transactionType,
                                        @Param("searchString") String searchString,
                                        Pageable pageable);


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

    @Query(value = "SELECT COALESCE(SUM(trees_consumed), 0) " +
            "FROM video_package_purchase " +
            "WHERE (:startDate IS NULL OR purchase_date >= :startDate) " +
            "AND (:endDate IS NULL OR purchase_date <= :endDate) AND is_refunded = false",
            nativeQuery = true)
    BigDecimal getTotalTreesTransactedForVideoPackages(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
