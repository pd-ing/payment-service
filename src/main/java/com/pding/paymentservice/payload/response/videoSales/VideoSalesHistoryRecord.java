package com.pding.paymentservice.payload.response.videoSales;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoSalesHistoryRecord {
    private String buyerEmail;
    private String nickname;
    private String videoTitle;
    private String amount;
    private String purchaseDate;
    private String duration;
    private String expiryDate;
    private String type;
    private String numberOfVideos;
    private Integer discountPercentage;
}
