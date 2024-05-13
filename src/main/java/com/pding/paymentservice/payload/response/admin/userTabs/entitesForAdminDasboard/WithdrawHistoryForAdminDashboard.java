package com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawHistoryForAdminDashboard {
    String createDateTime;

    String status;

    String applicationNumber;

    String rate;

    String actualPayment;

    String completeDate;
}
