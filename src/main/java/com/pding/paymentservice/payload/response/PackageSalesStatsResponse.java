package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response containing sales statistics for a video package
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageSalesStatsResponse {
    private String packageId;
    private Integer quantitySold;
    private BigDecimal totalTreesEarned;
}
