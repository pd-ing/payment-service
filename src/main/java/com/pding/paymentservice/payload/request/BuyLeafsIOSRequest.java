package com.pding.paymentservice.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyLeafsIOSRequest {

    String transactionId;

    String productId;

    public String getTransactionIdBase64Decoded() {
        byte[] decodedBytes = Base64.getDecoder().decode(this.transactionId);
        return new String(decodedBytes);
    }

    public String getProductIdBase64Decoded() {
        byte[] decodedBytes = Base64.getDecoder().decode(this.productId);
        return new String(decodedBytes);
    }
}
