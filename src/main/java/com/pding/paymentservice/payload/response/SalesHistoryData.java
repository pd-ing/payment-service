package com.pding.paymentservice.payload.response;

import com.pding.paymentservice.payload.response.videoSales.VideoSalesHistoryRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesHistoryData {
    ErrorResponse errorResponse;
    Long totalTreesEarned;
    List<VideoSalesHistoryRecord> videoSalesHistoryRecord;
}
