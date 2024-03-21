package com.pding.paymentservice.payload.response.admin.userTabs;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.VideoPurchaseHistoryForAdminDashboard;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public class ViewingHistory {
    BigDecimal totalVideosPurchasedInCurrentMonth;

    BigDecimal totalVideosViewedInCurrentMonth;

    BigDecimal treesUsedForVideoPurchaseInLastMonth;

    Page<VideoPurchaseHistoryForAdminDashboard> videoPurchaseHistoryForAdminDashboardList;

}
