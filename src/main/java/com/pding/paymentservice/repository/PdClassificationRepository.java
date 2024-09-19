package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PdClassificationRepository extends JpaRepository<VideoPurchase, String>  {

    @Query(value = "WITH TransactionCounts AS (\n" +
            "    SELECT pd_user_id, COUNT(DISTINCT id) AS transaction_count\n" +
            "    FROM call_purchase\n" +
            "    WHERE last_update_date >= NOW() - INTERVAL 30 DAY\n" +
            "    GROUP BY pd_user_id\n" +
            "    UNION ALL\n" +
            "    SELECT video_owner_user_id AS pd_user_id, COUNT(DISTINCT id) AS transaction_count\n" +
            "    FROM video_purchase\n" +
            "    WHERE last_update_date >= NOW() - INTERVAL 30 DAY\n" +
            "    GROUP BY video_owner_user_id\n" +
            "    UNION ALL\n" +
            "    SELECT pd_userid AS pd_user_id, COUNT(DISTINCT id) AS transaction_count\n" +
            "    FROM message_purchase\n" +
            "    -- WHERE last_update_date >= NOW() - INTERVAL 30 DAY\n" +
            "    GROUP BY pd_userid\n" +
            ")\n" +
            "SELECT pd_user_id, SUM(transaction_count) AS total_transactions\n" +
            "FROM TransactionCounts\n" +
            "GROUP BY pd_user_id\n" +
            "ORDER BY total_transactions DESC",
            countQuery = "SELECT COUNT(DISTINCT u.id) FROM users u " ,
            nativeQuery = true
    )
    Page<Object[]> findTopActiveUsers(Pageable pageable);
}
