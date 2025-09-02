package com.pding.paymentservice.payload.request;

import lombok.Data;

@Data
public class CheckLivestreamPurchaseRequest {
    private String buyerUserId;
    private String livestreamId;
}




