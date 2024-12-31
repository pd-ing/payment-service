package com.pding.paymentservice.payload.dto;

import com.pding.paymentservice.util.MapperUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSalesAndPurchaseNetCache implements Serializable {
    private static final long serialVersionUID = 1L;

    private String videoId;
    private Double treesEarned;
    private Long totalSales;

    public static VideoSalesAndPurchaseNetCache from(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        return MapperUtils.toObject(value, VideoSalesAndPurchaseNetCache.class);
    }
}
