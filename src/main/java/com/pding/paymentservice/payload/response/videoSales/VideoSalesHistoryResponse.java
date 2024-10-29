package com.pding.paymentservice.payload.response.videoSales;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoSalesHistoryResponse {
    ErrorResponse errorResponse;
    Long totalTreesEarned;
    Page<VideoSalesHistoryRecord> videoSalesHistoryRecord;
}
