package com.pding.paymentservice.models.other.services.tables.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferralInfoDTO {
    String id;

    String referrerPdUserId;

    String referredPdUserId;
}
