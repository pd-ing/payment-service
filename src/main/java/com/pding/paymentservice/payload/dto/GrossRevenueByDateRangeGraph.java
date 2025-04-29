package com.pding.paymentservice.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@Data
@AllArgsConstructor
public class GrossRevenueByDateRangeGraph {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal grossRevenue;
    private Map<LocalDate, BigDecimal> revenuePerDay;
}
