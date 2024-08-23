package com.pding.paymentservice.payload.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsVideoPurchasedByUserResponseV2 {
    String videoId;
    Boolean isPurchased;
    LocalDateTime expiryDate;
    Boolean isPermanent;
}
