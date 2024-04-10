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
    private String videoTitle;
    private String amount;
    private String purchaseDate;
}
