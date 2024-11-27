package com.pding.paymentservice.payload.response.donation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationHistoryResponse {
    String userEmailId;

    String userId;

    String profilePicture;

    String donatedTrees;

    String lastUpdateDate;
}
