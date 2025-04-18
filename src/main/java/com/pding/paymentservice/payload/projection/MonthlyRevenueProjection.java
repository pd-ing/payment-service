package com.pding.paymentservice.payload.projection;

import java.math.BigDecimal;

public interface MonthlyRevenueProjection {
    String getMonth();
    BigDecimal getRevenue();
}
