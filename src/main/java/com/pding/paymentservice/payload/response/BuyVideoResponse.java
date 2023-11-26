package com.pding.paymentservice.payload.response;


import com.pding.paymentservice.models.VideoPurchase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyVideoResponse {
    ErrorResponse errorResponse;
    VideoPurchase videoPurchase;
}
