package com.pding.paymentservice.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pding.paymentservice.service.ExposureTicketPurchaseService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SQSListeners {
    @Autowired
    ExposureTicketPurchaseService exposureTicketPurchaseService;
    @Autowired
    ObjectMapper objectMapper;


    @SqsListener("TopExposureSlotQueue")
    public void handleAutoExpireSlot(String message) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(message);
        exposureTicketPurchaseService.handleAutoExpireSlot(jsonNode.get("userId").asText());
    }

}
