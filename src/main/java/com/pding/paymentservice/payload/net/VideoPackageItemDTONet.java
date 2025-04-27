package com.pding.paymentservice.payload.net;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for video package item from content service
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoPackageItemDTONet {
    private String videoId;
    private String title;
    private String description;
    private BigDecimal permanentPrice;
    private String thumbnail;
    private Boolean isPurchased;
}
