package com.pding.paymentservice.payload.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PdSummaryDTO {
    private BigDecimal videoCallTotalDurationInSecond;
    private BigDecimal voiceCallTotalDurationInSecond;
    private Long totalTextMessages;
    private Long totalGifts;
    private String getStreamUserId;
    private BigDecimal totalHoldingLeafs;
    private BigDecimal totalHoldingTrees;
}
