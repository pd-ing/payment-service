package com.pding.paymentservice.payload.response.donation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationHistoryWithVideoStatsResponse extends DonationHistoryResponse {
    String totalVideosWatchedByUser;
    String totalVideosUploadedByPD;
    String recentDonation;
}
