package com.pding.paymentservice.payload.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LeafEarningInCallingHistoryDTO {
    private String callId;
    private String userId;
    private String pdId;
    private Float pricePerMinute;
    private String category;
    private Long durationInSeconds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalLeafs;
    private String email;
}
