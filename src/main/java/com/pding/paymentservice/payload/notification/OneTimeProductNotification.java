package com.pding.paymentservice.payload.notification;

import lombok.Data;

@Data
public class OneTimeProductNotification {
    private String version;
    private Integer notificationType;   // 1- ONE_TIME_PRODUCT_PURCHASED - A one-time product was successfully purchased by a user.
                                        // 2- ONE_TIME_PRODUCT_CANCELED - A pending one-time product purchase has been canceled by the user.
    private String purchaseToken;
    private String sku;

}
