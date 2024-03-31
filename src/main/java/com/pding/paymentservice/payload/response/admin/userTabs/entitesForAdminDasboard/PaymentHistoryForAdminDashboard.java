package com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard;

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
public class PaymentHistoryForAdminDashboard {
    String purchaseDate;

    String stripeId;

    String treeOrLeaf;

    String amount;

    String amountInDollarsWithTax;
}
