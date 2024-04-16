package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeSummary {
    private BigDecimal totalTreeRevenue;
    private BigDecimal totalTreesExchanged;
    private BigDecimal totalUnexchangedTrees;


}
