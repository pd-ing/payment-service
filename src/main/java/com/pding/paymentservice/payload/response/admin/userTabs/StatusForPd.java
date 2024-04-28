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
public class StatusForPd {
    BigDecimal totalTrees;
    BigDecimal exchnagedTrees;
    BigDecimal holdingTrees;
    BigDecimal revenueInCurrentMonth;
    BigDecimal mom;
    BigDecimal totalTreesEarnedInVideo;
    BigDecimal totalTreesEarnedInDonation;
}
