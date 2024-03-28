package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface GiftHistoryTabRepository extends JpaRepository<VideoPurchase, String> {

    @Query(value = "SELECT SUM(donated_trees) FROM donation " +
            "WHERE donor_user_id = :userId " +
            "AND YEAR(last_update_date) = YEAR(CURRENT_DATE) " +
            "AND MONTH(last_update_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
    BigDecimal totalTreesDonatedByUserInCurrentMonth(@Param("userId") String userId);

    @Query(value = "SELECT COALESCE(d.last_update_date, ''), " +
            "COALESCE('Tree', ''), " +
            "COALESCE(u.profile_id, ''), " +
            "COALESCE(d.donated_trees, '') " +
            "FROM donation d " +
            "LEFT JOIN users u ON d.donor_user_id = u.id " +
            "WHERE d.donor_user_id = ?1",
            countQuery = "SELECT COUNT(*) FROM donation d WHERE d.user_id = ?1",
            nativeQuery = true)
    Page<Object[]> findDonationHistoryByUserId(String userId, Pageable pageable);
}

