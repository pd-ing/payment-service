package com.pding.paymentservice.payload.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticTopSellPDResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String pdId;
    private BigDecimal totalTrees = BigDecimal.ZERO;
}
