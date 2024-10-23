package com.pding.paymentservice.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VideoSaleHistorySummary {
    private Long totalSale;
    private Long totalRePurchase;
}
