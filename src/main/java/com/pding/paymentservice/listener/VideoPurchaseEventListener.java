package com.pding.paymentservice.listener;

import com.pding.paymentservice.listener.event.VideoPurchaseEvent;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.projection.VideoProjection;
import com.pding.paymentservice.service.AsyncOperationService;
import com.pding.paymentservice.service.SendNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VideoPurchaseEventListener {
    @Autowired
    SendNotificationService sendNotificationService;

    @Autowired
    AsyncOperationService asyncOperationService;

    @EventListener
    @Async
    public void handleVideoPurchaseEvent(VideoPurchaseEvent event) {
        VideoPurchase videoPurchase = event.getVideoPurchase();
        VideoProjection videoData = event.getVideoData();


        sendNotificationService.sendBuyVideoNotification(videoPurchase, videoData.getVideoLibraryId());
        asyncOperationService.removeCachePattern("purchasedVideos::" + videoData.getUserId() + "," + videoPurchase.getUserId() + "*");
        asyncOperationService.removeCachePattern("videos::" + videoData.getUserId() + "," + videoPurchase.getUserId() + "*");
        asyncOperationService.removeCachePattern("videos::" + videoData.getUserId() + "," + videoData.getUserId() + "*");

    }
}
