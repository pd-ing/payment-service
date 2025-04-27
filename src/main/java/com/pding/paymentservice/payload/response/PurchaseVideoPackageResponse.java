package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response for a video package purchase
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseVideoPackageResponse {
    private String packagePurchaseId; // ID of the package purchase record
    private String packageId; // ID of the purchased package
    private String userId; // ID of the user who purchased the package
    private String sellerId; // ID of the seller who created the package
    private BigDecimal treesConsumed; // Amount paid for the package
    private LocalDateTime purchaseDate; // When the package was purchased
    private Set<String> includedVideoIds; // IDs of videos included in the purchase
    private Set<String> excludedVideoIds; // IDs of videos excluded from the purchase (already owned)
}
