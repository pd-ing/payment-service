package com.pding.paymentservice.payload.response.admin.userTabs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    BigDecimal totalTreesCharged;
    BigDecimal currentHoldingTrees;
    BigDecimal totalTreesSpendInVideoPurchase;
    BigDecimal totalTreesDonated;
    BigDecimal totalTreesSpent;
    BigDecimal treesAddedInCurrentMonth;
    BigDecimal mom;
    BigDecimal totalVideosPurchased;
}
