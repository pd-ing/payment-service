package com.pding.paymentservice.listener.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class VideoPackagePurchaseUpdatedEvent extends ApplicationEvent {
    private String packageId;

    public VideoPackagePurchaseUpdatedEvent(Object source, String packageId) {
        super(source);
        this.packageId = packageId;
    }


}
