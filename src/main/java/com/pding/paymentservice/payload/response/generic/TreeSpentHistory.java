package com.pding.paymentservice.payload.response.generic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TreeSpentHistory {
    private String lastUpdateDate;
    private String type;
    private String pdProfileId;
    private String amount;
}
