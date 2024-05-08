package com.pding.paymentservice.payload.request;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferralCommissionRequest {
    @Nullable
    String referralCommissionId;
}
