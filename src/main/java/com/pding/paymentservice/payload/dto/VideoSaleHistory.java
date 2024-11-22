package com.pding.paymentservice.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class VideoSaleHistory {
    private String videoId;
    private String videoOwnerUserId;
    private String buyerEmail;
    private String buyerId;
    private Integer numberOfPurchases;
    private List<VideoPurchaseLiteDTO> purchases;
}
