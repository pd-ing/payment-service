package com.pding.paymentservice.payload.response.admin;


import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.TreeSummary;
import com.pding.paymentservice.payload.response.admin.userTabs.PaymentHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.Status;
import com.pding.paymentservice.payload.response.admin.userTabs.ViewingHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.GiftHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardUserPaymentStats {
    ErrorResponse errorResponse;
    Status status;
    ViewingHistory viewingHistory;
    PaymentHistory paymentHistory;
    GiftHistory giftHistory;
    TreeSummary treeSummary;
    TreeSummaryGridResult treeSummaryGridResult;

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, Status status) {
        this.errorResponse = errorResponse;
        this.status = status;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, ViewingHistory viewingHistory) {
        this.errorResponse = errorResponse;
        this.viewingHistory = viewingHistory;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, PaymentHistory paymentHistory) {
        this.errorResponse = errorResponse;
        this.paymentHistory = paymentHistory;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, GiftHistory giftHistory) {
        this.errorResponse = errorResponse;
        this.giftHistory = giftHistory;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, TreeSummaryGridResult treeSummaryGridResult) {
        this.errorResponse = errorResponse;
        this.treeSummaryGridResult = treeSummaryGridResult;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, TreeSummary treesSummaryTotals) {
        this.errorResponse = errorResponse;
        this.treeSummary = treesSummaryTotals;
    }
}
