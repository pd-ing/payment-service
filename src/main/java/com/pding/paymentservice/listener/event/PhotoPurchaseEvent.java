package com.pding.paymentservice.listener.event;

import com.pding.paymentservice.models.PhotoPurchase;
import com.pding.paymentservice.payload.net.PhotoPostResponseNet;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PhotoPurchaseEvent extends ApplicationEvent {
    private PhotoPurchase photoPurchase;
    private PhotoPostResponseNet post;

    public PhotoPurchaseEvent(Object source, PhotoPurchase photoPurchase, PhotoPostResponseNet post) {
        super(source);
        this.photoPurchase = photoPurchase;
        this.post = post;
    }


}
