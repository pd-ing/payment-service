package com.pding.paymentservice.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pding.paymentservice.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Service
@Slf4j
public class DonationEventSnsPublisher extends BaseService {

    @Autowired
    private SnsClient snsClient;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aws.sns.donation-topic-arn}")
    private String donationTopicArn;

    public void publishDonationEvent(
            String donorUserId,
            String pdUserId, 
            String donorUserEmail,
            BigDecimal donatedTrees,
            BigDecimal donatedLeafs
    ) {
        try {
            HashMap<String, Object> eventData = new HashMap<>();
            eventData.put("eventType", "DONATION_CREATED");
            eventData.put("donorUserId", donorUserId);
            eventData.put("pdUserId", pdUserId);
            eventData.put("donorUserEmail", donorUserEmail);
            eventData.put("donatedTrees", donatedTrees);
            eventData.put("donatedLeafs", donatedLeafs);
            eventData.put("timestamp", new Date());
            
            String messageBody = objectMapper.writeValueAsString(eventData);
            
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(donationTopicArn) // <-- FIX: Use the injected variable here
                    .message(messageBody)
                    .subject("DonationEvent")
                    // FIFO-specific attributes
                    .messageGroupId("donation-events") // Groups related messages
                    .messageDeduplicationId(generateDeduplicationId(donorUserId, pdUserId)) // Prevents duplicates
                    .build();
                    
            PublishResponse response = snsClient.publish(publishRequest);
            log.info("Published donation event to FIFO SNS. MessageId: {}", response.messageId());
            
        } catch (Exception e) {
            pdLogger.logException(e);
            log.error("Failed to publish donation event to FIFO SNS", e);
        }
    }

    private String generateDeduplicationId(String donorUserId, String pdUserId) {
        // Generate a unique deduplication ID based on donor, PD, and timestamp
        // This prevents duplicate donations within the deduplication interval (5 minutes)
        long timeWindow = System.currentTimeMillis() / (5 * 60 * 1000); // 5-minute window
        return String.format("%s-%s-%d", donorUserId, pdUserId, timeWindow);
    }
}