package com.pding.paymentservice.payload.response.admin.userTabs;

import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.DonationHistoryForAdminDashboard;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public class GiftHistory {
    BigDecimal totalTreesDonatedInCurrentMonth;

    Page<DonationHistoryForAdminDashboard> donationHistoryForAdminDashboardList;
}
