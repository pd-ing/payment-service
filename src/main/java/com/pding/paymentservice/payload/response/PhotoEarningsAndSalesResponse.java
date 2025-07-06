package com.pding.paymentservice.payload.response;

import com.pding.paymentservice.models.tables.inner.PhotoEarningsAndSales;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoEarningsAndSalesResponse {
    ErrorResponse errorResponse;
    Map<String, PhotoEarningsAndSales> photoEarningsAndSales;
}
