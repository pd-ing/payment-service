package com.pding.paymentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a purchase of a video package.
 */
@Entity
@Table(name = "video_package_purchase")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class VideoPackagePurchase {
    @Id
    @UuidGenerator
    private String id;

    private String userId; // The user who purchased the package

    private String packageId; // ID of the purchased package

    private String sellerId; // The creator who created the package

    private BigDecimal treesConsumed; // Total amount paid for the package

    private LocalDateTime purchaseDate; // When the package was purchased

    @Column(columnDefinition = "text")
    private String includedVideoIds; // Comma-separated list of video IDs included in the purchase

    @Column(columnDefinition = "text")
    private String excludedVideoIds; // Comma-separated list of video IDs excluded from the purchase (already owned)

    private BigDecimal originalPrice; // Original price before discount

    private Integer discountPercentage; // Discount percentage applied

    @Column(columnDefinition = "boolean default false")
    private Boolean isRefunded = false; // Whether the purchase has been refunded

    /**
     * Constructor for creating a new package purchase
     * @param userId The user who purchased the package
     * @param packageId ID of the purchased package
     * @param sellerId The creator who created the package
     * @param treesConsumed Total amount paid for the package
     * @param includedVideoIds List of video IDs included in the purchase
     * @param excludedVideoIds List of video IDs excluded from the purchase (already owned)
     * @param originalPrice Original price before discount
     * @param discountPercentage Discount percentage applied
     */
    public VideoPackagePurchase(String userId, String packageId, String sellerId, BigDecimal treesConsumed,
                               List<String> includedVideoIds, List<String> excludedVideoIds,
                               BigDecimal originalPrice, Integer discountPercentage) {
        this.userId = userId;
        this.packageId = packageId;
        this.sellerId = sellerId;
        this.treesConsumed = treesConsumed;
        this.purchaseDate = LocalDateTime.now();
        this.includedVideoIds = String.join(",", includedVideoIds);
        this.excludedVideoIds = excludedVideoIds != null && !excludedVideoIds.isEmpty() ?
                                String.join(",", excludedVideoIds) : null;
        this.originalPrice = originalPrice;
        this.discountPercentage = discountPercentage;
        this.isRefunded = false;
    }

    /**
     * Gets the list of video IDs included in the purchase
     * @return List of video IDs
     */
    public List<String> getIncludedVideoIdsList() {
        if (includedVideoIds == null || includedVideoIds.isEmpty()) {
            return List.of();
        }
        return List.of(includedVideoIds.split(","));
    }

    /**
     * Gets the list of video IDs excluded from the purchase (already owned)
     * @return List of video IDs
     */
    public List<String> getExcludedVideoIdsList() {
        if (excludedVideoIds == null || excludedVideoIds.isEmpty()) {
            return List.of();
        }
        return List.of(excludedVideoIds.split(","));
    }
}
