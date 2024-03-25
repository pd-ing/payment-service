package com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentHistoryForAdminDashboard {
    LocalDateTime purchaseDate;

    String stripeId;

    String treeOrLeaf;

    BigDecimal amount;

    String amountInDollarsWithTax;
}
