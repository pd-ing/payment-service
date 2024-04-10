package com.pding.paymentservice.payload.response.videoSales;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoSalesHistoryResponse {
    ErrorResponse errorResponse;
    List<VideoSalesHistoryRecord> videoSalesHistoryRecord;
}
