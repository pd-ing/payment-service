package com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard;

import com.pding.paymentservice.models.enums.WithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawHistoryForAdminDashboard {
    String createDateTime;

    WithdrawalStatus status;

    BigDecimal applicationNumber;

    String rate;

    BigDecimal actualPayment;

    String completeDate;
}
