package com.pding.paymentservice.payload.response;

import com.pding.paymentservice.models.VideoPurchase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetVideoTransactionsResponse {
    ErrorResponse errorResponse;
    List<VideoPurchase> videoPurchaseList;
    Boolean hasNext;
}
