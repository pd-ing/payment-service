package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckLivestreamPurchaseResponse {
    private boolean purchased;
    private String message;
    private String purchaseId;
}




