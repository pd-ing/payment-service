package com.pding.paymentservice.payload.response.videoSales;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoSalesHistoryRecord {
    private String buyerEmail;
    private String videoTitle;
    private String amount;
    private String purchaseDate;
    private String duration;
    private String expiryDate;
    private String type;
    private String numberOfVideos;
    private Integer discountPercentage;
}
