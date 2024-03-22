package com.pding.paymentservice.payload.response.admin.userTabs;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.VideoPurchaseHistoryForAdminDashboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewingHistory {
    BigDecimal totalVideosPurchasedInCurrentMonth;

    BigDecimal totalVideosViewedInCurrentMonth;

    BigDecimal treesUsedForVideoPurchaseInLastMonth;

    Page<VideoPurchaseHistoryForAdminDashboard> videoPurchaseHistoryForAdminDashboardList;

}
