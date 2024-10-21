package com.pding.paymentservice.payload.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LeafGiftHistoryDTO {
    private String pdId;
    private String userId;
    private String userEmail;
    private LocalDateTime date;
    private BigDecimal leafsTransacted;
}
