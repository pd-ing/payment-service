package com.pding.paymentservice.models.other.services.tables.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    String id;

    String email;

    String pdType;

    String nickname;

    String commissionPercent;

    String linkedStripeId;
}
