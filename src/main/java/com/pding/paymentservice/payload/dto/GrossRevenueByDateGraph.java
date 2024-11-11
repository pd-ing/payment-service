package com.pding.paymentservice.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
public class GrossRevenueByDateGraph {
    private LocalDate date;
    private BigDecimal grossRevenue;
    //map revenue per hour of selected date
    private Map<Integer, BigDecimal> revenuePerHour;
}
