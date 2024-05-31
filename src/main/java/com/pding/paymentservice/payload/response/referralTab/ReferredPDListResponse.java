package com.pding.paymentservice.payload.response.referralTab;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.videoSales.VideoSalesHistoryRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferredPDListResponse {

    ErrorResponse errorResponse;
    Page<ReferredPDDetailsRecord> referredPDDetailsRecords;
}



