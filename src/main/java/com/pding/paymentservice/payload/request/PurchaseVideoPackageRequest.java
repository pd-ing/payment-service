package com.pding.paymentservice.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request for purchasing a video package
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseVideoPackageRequest {
    private String packageId; // ID of the package to purchase
    private String sellerId; // ID of the seller who created the package
    private BigDecimal personalizedPrice; // Personalized price based on user's owned videos
    private BigDecimal originalPrice; // Original price before discount
    private Integer discountPercentage; // Discount percentage applied
    private List<String> videoIds; // IDs of all videos in the package
    private List<String> ownedVideoIds; // IDs of videos the user already owns
}
