package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class BuyMissionResponse {
    private boolean success;
    private String message;
    private String purchaseId;
}
