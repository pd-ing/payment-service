package com.pding.paymentservice.models.other.services.tables.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VideoDurationPriceDTO {
    private String videoId;
    private String duration;
    private BigDecimal trees;
    private Boolean enabled;

}
