package com.pding.paymentservice.listener;

import com.pding.paymentservice.listener.event.PhotoPurchaseEvent;
import com.pding.paymentservice.models.PhotoPurchase;
import com.pding.paymentservice.payload.net.PhotoPostResponseNet;
import com.pding.paymentservice.service.SendNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PhotoPurchaseEventListener {
    @Autowired
    SendNotificationService sendNotificationService;

    @EventListener
    @Async
    public void handleVideoPurchaseEvent(PhotoPurchaseEvent event) {
        PhotoPurchase photoPurchase = event.getPhotoPurchase();
        PhotoPostResponseNet post = event.getPost();
        sendNotificationService.sendBuyPhotoNotification(photoPurchase, post.getTitle());
    }
}
