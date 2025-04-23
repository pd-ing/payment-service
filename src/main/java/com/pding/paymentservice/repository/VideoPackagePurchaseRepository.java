package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPackagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for video package purchase operations
 */
@Repository
public interface VideoPackagePurchaseRepository extends JpaRepository<VideoPackagePurchase, String> {

    /**
     * Find all purchases by user ID
     * @param userId The user ID
     * @return List of package purchases
     */
    List<VideoPackagePurchase> findByUserId(String userId);

    /**
     * Find all purchases by seller ID
     * @param sellerId The seller ID
     * @return List of package purchases
     */
    List<VideoPackagePurchase> findBySellerId(String sellerId);

    /**
     * Find a purchase by user ID and package ID
     * @param userId The user ID
     * @param packageId The package ID
     * @return The package purchase if found
     */
    Optional<VideoPackagePurchase> findByUserIdAndPackageId(String userId, String packageId);

    /**
     * Check if a user has already purchased a package
     * @param userId The user ID
     * @param packageId The package ID
     * @return True if the user has purchased the package
     */
    boolean existsByUserIdAndPackageId(String userId, String packageId);

    /**
     * Find all purchases by seller ID in a date range
     * @param sellerId The seller ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of package purchases
     */
    @Query("SELECT p FROM VideoPackagePurchase p WHERE p.sellerId = :sellerId AND p.purchaseDate BETWEEN :startDate AND :endDate")
    List<VideoPackagePurchase> findBySellerIdAndDateRange(
            @Param("sellerId") String sellerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
