package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.WalletHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PaymentHistoryTabRepository extends JpaRepository<WalletHistory, String> {

    @Query(value = "SELECT COALESCE(SUM(purchased_trees), 0) FROM  wallet_history" +
            "WHERE user_id = :userId " +
            "AND YEAR(purchase_date) = YEAR(CURRENT_DATE) " +
            "AND MONTH(purchase_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
    BigDecimal numberOfTreesChargedInCurrentMonth(@Param("userId") String userId);

    @Query(value = "SELECT COALESCE(ISNULL(u.linked_stripe_id,'Stripe ID not set'), ''), " +
            "COALESCE(wh.purchase_date, ''), " +
            "COALESCE(wh.purchased_trees, ''), " +
            "COALESCE(wh.purchased_leafs, ''), " +
            "COALESCE(wh.amount, ''), " +
            "FROM wallet_history wh " +
            "LEFT JOIN users u ON wh.user_id = u.id " +
            "WHERE wh.user_id = ?1",
            countQuery = "SELECT COUNT(*) FROM wallet_history wh WHERE wh.user_id = ?1",
            nativeQuery = true)
    Page<Object[]> findPayentHistoryByUserId(String userId, Pageable pageable);

}

