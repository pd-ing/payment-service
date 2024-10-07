package com.pding.paymentservice.app.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeafItem {
    private String giftId;
    private int leafAmount;
    private int treeAmount;
    private String imageUrl;
    private String animationUrl;

    public LeafItem(String giftId, int leafAmount, String imageUrl, String animationUrl) {
        this.giftId = giftId;
        this.leafAmount = leafAmount;
        this.imageUrl = imageUrl;
        this.animationUrl = animationUrl;
    }

    // leaf amount and tree amount are the same
    public int getTreeAmount() {
        return leafAmount;
    }


}
