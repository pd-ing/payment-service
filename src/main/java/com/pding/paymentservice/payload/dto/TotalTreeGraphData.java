package com.pding.paymentservice.payload.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class TotalTreeGraphData {
    ZonedDateTime time;
    BigDecimal totalTreeOfUser;
    BigDecimal totalPDTreeOfPd;
}
