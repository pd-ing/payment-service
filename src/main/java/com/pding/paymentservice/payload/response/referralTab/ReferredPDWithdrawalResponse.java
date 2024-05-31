package com.pding.paymentservice.payload.response.referralTab;

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
public class ReferredPDWithdrawalResponse {

    ErrorResponse errorResponse;
    Page<ReferredPDWithdrawalRecord> referredPDWithdrawalRecords;
}
