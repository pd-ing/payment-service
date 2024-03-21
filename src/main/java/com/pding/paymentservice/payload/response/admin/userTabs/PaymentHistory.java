package com.pding.paymentservice.payload.response.admin.userTabs;

import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.PaymentHistoryForAdminDashboard;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public class PaymentHistory {
    BigDecimal numberOfTreesChargedInCurrentMonth;

    Page<PaymentHistoryForAdminDashboard> paymentHistoryForAdminDashboardList;
}
