package com.pding.paymentservice.listener.event;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.projection.VideoProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

@Getter
public class VideoPurchaseEvent extends ApplicationEvent {
    private VideoPurchase videoPurchase;
    private VideoProjection videoData;

    public VideoPurchaseEvent(Object source, VideoPurchase videoPurchase, VideoProjection videoData) {
        super(source);
        this.videoPurchase = videoPurchase;
        this.videoData = videoData;
    }


}
