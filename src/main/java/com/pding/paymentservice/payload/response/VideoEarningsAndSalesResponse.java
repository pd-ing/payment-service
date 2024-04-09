package com.pding.paymentservice.payload.response;

import com.pding.paymentservice.models.tables.inner.VideoEarningsAndSales;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoEarningsAndSalesResponse {
    ErrorResponse errorResponse;
    Map<String, VideoEarningsAndSales> videoEarningsAndSales;
}
