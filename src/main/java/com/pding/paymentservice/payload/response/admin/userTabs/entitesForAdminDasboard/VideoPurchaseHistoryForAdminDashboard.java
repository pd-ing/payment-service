package com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VideoPurchaseHistoryForAdminDashboard {
    LocalDateTime purchasedDate;

    String videoId;

    String videoThumbnail;

    String videoTitle;

    String pdProfileId;

    BigDecimal videoPrice;
}
