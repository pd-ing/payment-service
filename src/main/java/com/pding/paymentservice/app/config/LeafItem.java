package com.pding.paymentservice.app.config;

import com.pding.paymentservice.app.config.enums.LeafType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeafItem {
    private int giftId;
    private int leafAmount;
    private String imageUrl;
    private String animationUrl;
}
