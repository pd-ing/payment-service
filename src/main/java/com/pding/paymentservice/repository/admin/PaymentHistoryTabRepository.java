package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.WalletHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentHistoryTabRepository extends JpaRepository<WalletHistory, String> {

    @Query(value = "SELECT COALESCE(SUM(purchased_trees), 0) FROM  wallet_history " +
            "WHERE user_id = :userId " +
            "AND YEAR(purchase_date) = YEAR(CURRENT_DATE) " +
            "AND MONTH(purchase_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
    BigDecimal numberOfTreesChargedInCurrentMonth(@Param("userId") String userId);

    @Query(value = "SELECT COALESCE(u.linked_stripe_id,'Stripe ID not set') " +
            "FROM users u " +
            "WHERE id = :userId ", nativeQuery = true)
    String userStripeID(@Param("userId") String userId);

    @Query(value = "SELECT COALESCE(wh.purchase_date, ''), " +
            "COALESCE(wh.purchased_trees, '0.0'), " +
            "COALESCE(wh.purchased_leafs, '0.0'), " +
            "COALESCE(wh.amount, '0.0'), " +
            "' ' AS email " + //return empty email in this case
            "FROM wallet_history wh " +
            "WHERE wh.user_id = ?1 " +
            "ORDER BY wh.purchase_date",
            countQuery = "SELECT COUNT(*) FROM wallet_history wh WHERE wh.user_id = ?1",
            nativeQuery = true)
    Page<Object[]> findPaymentHistoryByUserId(String userId, Pageable pageable);

    @Query(value = "SELECT COALESCE(wh.purchase_date, ''), " +
            "COALESCE(wh.purchased_trees, '0.0'), " +
            "COALESCE(wh.purchased_leafs, '0.0'), " +
            "COALESCE(wh.amount, '0.0'), " +
            "COALESCE(u.email, '') " +
            "FROM wallet_history wh " +
            "LEFT JOIN users u ON wh.user_id = u.id " +
            "WHERE (:startDate IS NULL OR wh.purchase_date >= :startDate) " +
            "AND (:endDate IS NULL OR wh.purchase_date <= :endDate) ",
            countQuery = "SELECT COUNT(*) FROM wallet_history wh WHERE (:startDate IS NULL OR wh.purchase_date >= :startDate) "+
                    "AND (:endDate IS NULL OR wh.purchase_date <= :endDate)",
            nativeQuery = true)
    Page<Object[]> getPaymentHistoryForAllUsers(LocalDate startDate, LocalDate endDate, Pageable pageable);

}

