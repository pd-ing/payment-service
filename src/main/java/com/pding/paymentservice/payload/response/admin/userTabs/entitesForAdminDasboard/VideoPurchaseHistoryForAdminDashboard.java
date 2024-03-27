package com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoPurchaseHistoryForAdminDashboard {
    String purchasedDate;

    String videoId;

    String videoThumbnail;

    String videoTitle;

    String pdProfileId;

    String videoPrice;
}
