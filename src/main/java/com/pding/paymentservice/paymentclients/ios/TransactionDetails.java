package com.pding.paymentservice.paymentclients.ios;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class TransactionDetails {

    private String transactionId;
    private String originalTransactionId;
    private String bundleId;
    private String productId;
    private long purchaseDate;
    private long originalPurchaseDate;
    private int quantity;
    private String type;
    private String inAppOwnershipType;
    private long signedDate;
    private String environment;
    private String transactionReason;
    private String storefront;
    private String storefrontId;
    private BigDecimal leafs;
    private BigDecimal price;
    private String currency;

}
