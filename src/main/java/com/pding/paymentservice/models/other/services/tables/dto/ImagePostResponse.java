package com.pding.paymentservice.models.other.services.tables.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ImagePostResponse {
    String postId;
    String title;
    String description;
    Boolean isPaid;
    Boolean isAdult;
    Boolean isVisible;
    String userId;
    String lastUpdated;
    BigDecimal leafAmount;
    Integer imageCount;
    Boolean isPurchased;
    List<String> images;

}
