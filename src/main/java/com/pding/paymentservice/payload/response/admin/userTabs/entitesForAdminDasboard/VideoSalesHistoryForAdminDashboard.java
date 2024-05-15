package com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoSalesHistoryForAdminDashboard {
    String videoId;

    String videoTitle;

    String views;

    String salePrice;

    String profit;

    String date;

}
