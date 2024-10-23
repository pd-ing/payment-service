package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoSaleHistory {
    private String videoId;
    private String videoOwnerUserId;
    private LocalDateTime dateOfPurchase;
    private String buyerEmail;
    private String duration;
    private LocalDateTime expiryDate;
    private BigDecimal treesConsumed;

}
