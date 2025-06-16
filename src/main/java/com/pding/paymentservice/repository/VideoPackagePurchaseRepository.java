package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPackagePurchase;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    boolean existsByUserIdAndPackageIdAndIsRefundedFalse(String userId, String packageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "from VideoPackagePurchase  where userId = :userId and packageId = :packageId and isRefunded = false")
    List<VideoPackagePurchase> findUnrefundedPackageByUserIdAndPackageIdForUpdate(String userId, String packageId);

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

    List<VideoPackagePurchase> findAllByUserIdAndPackageIdInAndIsRefundedFalse(String userId, Set<String> packageIds);

    Page<VideoPackagePurchase> findByPackageIdAndSellerIdAndIsRefundedFalse(String packageId, String sellerId, Pageable pageable);


    @Query(value = " select vpp.*, vpp.purchase_date as purchaseDate" +
                   " from video_package_purchase vpp" +
                   "          join users buyer on vpp.user_id = buyer.id" +
                   " where vpp.package_id = :packageId" +
                   "   and seller_id = :sellerId" +
                   "   and is_refunded = false" +
                   "   and (buyer.nickname like concat(:searchString, '%') or buyer.email like concat(:searchString, '%'))",
    countQuery = " select count(distinct vpp.id)" +
                 " from video_package_purchase vpp" +
                 "          join users buyer on vpp.user_id = buyer.id" +
                 " where vpp.package_id = :packageId" +
                 "   and seller_id = :sellerId" +
                 "   and is_refunded = false" +
                 "   and (buyer.nickname like concat(:searchString, '%') or buyer.email like concat(:searchString, '%'))"
            , nativeQuery = true)
    Page<VideoPackagePurchase> findByPackageIdAndSellerIdAndIsRefundedFalse(String packageId, String sellerId, String searchString, Pageable pageable);

    /**
     * Find all non-refunded purchases for a package
     * @param packageId The package ID
     * @return List of package purchases
     */
    List<VideoPackagePurchase> findByPackageIdAndIsRefundedFalse(String packageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "from VideoPackagePurchase  where packageId = :packageId and isRefunded = false")
    List<VideoPackagePurchase> findByPackageIdAndIsRefundedFalseForUpdate(String packageId);

    /**
     * Find all non-refunded purchases for a list of package IDs
     * @param packageIds The list of package IDs
     * @return List of package purchases
     */
    List<VideoPackagePurchase> findByPackageIdInAndIsRefundedFalse(List<String> packageIds);

    @Query("SELECT p FROM VideoPackagePurchase p WHERE p.packageId = :packageId AND p.purchaseDate >= :startDate AND p.purchaseDate <= :endDate AND p.isRefunded = false")
    List<VideoPackagePurchase> findByPackageIdAndPurchaseDateBetweenAndIsRefundedFalse(
            String packageId, LocalDateTime startDate, LocalDateTime endDate);


    @Query(value = "SELECT COALESCE(SUM(p.trees_consumed) - SUM(p.drm_fee), 0) FROM video_package_purchase p WHERE p.seller_id = :pdId AND p.purchase_date BETWEEN :startDate AND :endDate AND p.is_refunded = false", nativeQuery = true)
    Long getTotalTreesEarnedFromPackageSales(@Param("pdId") String pdId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
