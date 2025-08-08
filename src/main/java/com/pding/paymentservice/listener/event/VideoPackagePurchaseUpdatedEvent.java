package com.pding.paymentservice.listener.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class VideoPackagePurchaseUpdatedEvent extends ApplicationEvent {
    private String packageId;
    private String sellerId;
    private String packageTitle;
    private BigDecimal tree;
    private String type;
    private String buyerId;
//    private String buyerName;

    public VideoPackagePurchaseUpdatedEvent(Object source, String packageId) {
        super(source);
        this.packageId = packageId;
    }

    public VideoPackagePurchaseUpdatedEvent(Object source, String packageId, String sellerId, String packageTitle, BigDecimal tree, String type, String buyerId) {
        super(source);
        this.packageId = packageId;
        this.sellerId = sellerId;
        this.packageTitle = packageTitle;
        this.tree = tree;
        this.type = type;
        this.buyerId = buyerId;
//        this.buyerName = buyerName;
    }


}
