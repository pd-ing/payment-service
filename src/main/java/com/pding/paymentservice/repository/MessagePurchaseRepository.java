package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.MessagePurchase;
import com.pding.paymentservice.payload.projection.MonthlyRevenueProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessagePurchaseRepository extends JpaRepository<MessagePurchase, String> {

    @Query(nativeQuery = true, value =
            " SELECT DATE_FORMAT(mp.last_update_date, '%Y-%m') AS month," +
            "        COALESCE(SUM(mp.trees_transacted), 0)                    as revenue" +
            " FROM message_purchase mp" +
            " WHERE mp.pd_userid = :pdId" +
//            "   AND mp.last_update_date >= DATE_SUB(CURRENT_DATE, INTERVAL 3 MONTH)" +
            " GROUP BY DATE_FORMAT(mp.last_update_date, '%Y-%m')" +
            " ORDER BY month DESC" +
            " LIMIT :limit"
    )
    List<MonthlyRevenueProjection> getMonthlyRevenueFromMessagePurchaseByUserId(@Param("pdId") String pdId, @Param("limit") Integer limit);
}
