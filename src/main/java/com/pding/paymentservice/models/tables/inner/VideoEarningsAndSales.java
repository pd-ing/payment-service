package com.pding.paymentservice.models.tables.inner;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoEarningsAndSales {

    BigDecimal treesEarned;

    Long totalSales;
}
