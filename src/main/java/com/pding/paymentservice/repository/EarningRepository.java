package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Earning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface EarningRepository extends JpaRepository<Earning, String> {
    Optional<Earning> findByUserId(String userId);

    // Find earnings ordered by the sum of treesEarned and leafsEarned in descending order
    @Query("SELECT e FROM Earning e ORDER BY (e.treesEarned + e.leafsEarned) DESC")
    Page<Earning> findAllOrderByTotalEarningsDesc(Pageable pageable);

    // Query method to get the sum of all treesEarned
    @Query("SELECT COALESCE(SUM(e.treesEarned), 0) FROM Earning e")
    BigDecimal sumOfAllTreesEarned();

    @Query("SELECT COALESCE(SUM(e.leafsEarned), 0) FROM Earning e")
    BigDecimal sumOfAllLeafsEarned();
}
