package com.pding.paymentservice.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Request for purchasing a video package
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckPurchaseVideoPackageRequest {
    private String buyerId;
    private Set<String> packageIds;
}
