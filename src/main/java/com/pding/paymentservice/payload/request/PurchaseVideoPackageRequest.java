package com.pding.paymentservice.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Request for purchasing a video package
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseVideoPackageRequest {
    private String packageId; // ID of the package to purchase
    private Set<String> selectedVideoIds; // IDs of videos to purchase (used for FREE_CHOICE_PACKAGE)
}
