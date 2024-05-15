package com.pding.paymentservice.payload.response.admin.userTabs;

import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.WithdrawHistoryForAdminDashboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawHistoryForPd {
    String pdStripeId;

    Page<WithdrawHistoryForAdminDashboard> withdrawHistoryForAdminDashboardList;
}
