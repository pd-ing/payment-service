package com.pding.paymentservice.payload.dto;

import com.pding.paymentservice.payload.net.VideoPackageItemDTONet;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VideoPackagePurchaseDTO {
    private String id;
    private String userId;
    private String email;
    private String packageId;
    private String sellerId;
    private BigDecimal treesConsumed;
    private LocalDateTime purchaseDate;
    private List<String> includedVideoIds;
    private List<String> excludedVideoIds;
    private List<VideoPackageItemDTONet> includedVideos;
    private List<VideoPackageItemDTONet> excludedVideos;
    private BigDecimal originalPrice;
    private Integer discountPercentage;
    private Boolean isRefunded;
}
