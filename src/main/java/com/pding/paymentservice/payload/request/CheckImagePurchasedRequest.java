package com.pding.paymentservice.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class CheckImagePurchasedRequest {
    private List<String> postIds;
}
