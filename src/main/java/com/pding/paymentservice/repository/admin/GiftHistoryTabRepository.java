package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface GiftHistoryTabRepository extends JpaRepository<VideoPurchase, String> {

    @Query(value = "SELECT SUM(donated_trees) FROM donation " +
            "WHERE donor_user_id = :userId " +
            "AND YEAR(last_update_date) = YEAR(CURRENT_DATE) " +
            "AND MONTH(last_update_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
    BigDecimal totalTreesDonatedByUserInCurrentMonth(@Param("userId") String userId);

    @Query(value = "SELECT COALESCE(d.last_update_date, ''), " +
            "COALESCE(u.profile_id, ''), " +
            "COALESCE('Tree', ''), " +
            "COALESCE(d.donated_trees, ''), " +
            "COALESCE(u.email, '') " +
            "FROM donation d " +
            "LEFT JOIN users u ON d.pd_user_id = u.id " +
            "WHERE d.donor_user_id = ?1",
            countQuery = "SELECT COUNT(*) FROM donation d WHERE d.donor_user_id = ?1",
            nativeQuery = true)
    Page<Object[]> findDonationHistoryByUserId(String userId, Pageable pageable);

    @Query(value = "SELECT SUM(donated_trees) FROM donation " +
            "WHERE pd_user_id = :pdUserId ", nativeQuery = true)
    BigDecimal totalTreesReceivedByPd(@Param("pdUserId") String pdUserId);

    @Query(value = "SELECT COALESCE(d.last_update_date, ''), " +
            "COALESCE(u.profile_id, ''), " +
            "COALESCE('Tree', ''), " +
            "COALESCE(d.donated_trees, '0.0'), " +
            "COALESCE(u.email, '') " +
            "FROM donation d " +
            "LEFT JOIN users u ON d.pd_user_id = u.id " +
            "WHERE d.pd_user_id = :pdUserId " +
            "AND (:startDate IS NULL OR d.last_update_date >= :startDate) " +
            "AND (:endDate IS NULL OR d.last_update_date <= :endDate) ",
            countQuery = "SELECT COUNT(*) FROM donation d WHERE d.pd_user_id = :pdUserId " +
                    "AND (:startDate IS NULL OR d.last_update_date >= :startDate) AND (:endDate IS NULL OR d.last_update_date <= :endDate) ",
            nativeQuery = true)
    Page<Object[]> findDonationHistoryByPdId(String pdUserId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
}

