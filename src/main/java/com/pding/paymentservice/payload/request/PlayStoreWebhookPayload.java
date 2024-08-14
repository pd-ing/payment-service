package com.pding.paymentservice.payload.request;

import lombok.Data;

import java.util.Map;

@Data
public class PlayStoreWebhookPayload {
//    {
//        "data": "base64EncodedString",
//            "messageId": "11951459171423584",
//            "message_id": "11951459171423584",
//            "publishTime": "2024-08-08T05:31:04.288Z",
//            "publish_time": "2024-08-08T05:31:04.288Z"
//    }
    Map<String, String> message;

    String subscription;
}
