package com.pding.paymentservice.listener.event;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.projection.VideoProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPurchaseEvent {
    private VideoPurchase videoPurchase;
    private VideoProjection videoData;


}
