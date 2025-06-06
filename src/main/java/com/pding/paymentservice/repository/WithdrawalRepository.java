package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, String> {
    List<Withdrawal> findByPdUserIdAndStatus(String pdUserId, WithdrawalStatus status);

    @Query("SELECT w FROM Withdrawal w WHERE w.pdUserId = :pdUserId and date(w.createdDate) = current_date")
    List<Withdrawal> findTodayWithdrawalByPdUserId(String pdUserId);

    List<Withdrawal> findByStatus(WithdrawalStatus status);

    List<Withdrawal> findByPdUserId(String pdUserId);

    List<Withdrawal> findByPdUserIdOrderByCreatedDateDesc(String pdUserId);

    @Query(value = "SELECT COALESCE(u.linked_stripe_id, '') FROM users u " +
            "WHERE id = :pdUserId ", nativeQuery = true)
    String getPdStripeId(String pdUserId);


    @Query(value = "SELECT COALESCE(w.created_date, ''), COALESCE(w.status, ''), COALESCE(w.trees, ''), \n" +
            "            CASE WHEN u.pd_type = 'GENERAL' THEN 80 \n" +
            "            WHEN u.pd_type = 'BEST' THEN 85 \n" +
            "            WHEN u.pd_type = 'PARTNER' THEN 90 \n" +
            "            ELSE 0 END rate, \n" +
            "            CASE WHEN w.status = 'PENDING' THEN '-' ELSE COALESCE(w.updated_date, '') END updateDate \n" +
            "            FROM withdrawals w \n" +
            "            INNER JOIN users u ON w.pd_user_id = u.id \n" +
            "            WHERE w.pd_user_id = :pdUserId \n" +
            "           AND (:startDate  IS NULL OR w.created_date >= :startDate ) \n" +
            "            AND (:endDate IS NULL OR w.created_date <= :endDate) ",
            countQuery = "SELECT COUNT(*) FROM withdrawals w INNER JOIN users u ON w.pd_user_id = u.id WHERE w.pd_user_id = :pdUserId " +
                    " AND (:startDate  IS NULL OR w.created_date >= :startDate ) AND (:endDate IS NULL OR w.created_date <= :endDate)",
            nativeQuery = true)
    Page<Object[]> findWithdrawalHistoryByPdId(String pdUserId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query(value = "SELECT w.*, w.created_date as createdDate" +
            "            FROM withdrawals w" +
            "            INNER JOIN users u ON w.pd_user_id = u.id" +
            "           WHERE u.email like concat(:searchString, '%') OR u.nickname like concat(:searchString, '%')",
            countQuery = "SELECT COUNT(*) FROM withdrawals w INNER JOIN users u ON w.pd_user_id = u.id WHERE u.email like concat(:searchString, '%') OR u.nickname like concat(:searchString, '%')",
            nativeQuery = true)
    Page<Withdrawal> findAllWithdrawals(String searchString, Pageable pageable);
}
