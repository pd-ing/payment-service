package com.pding.paymentservice.payload.response.admin;


import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.TreeSummary;
import com.pding.paymentservice.payload.response.admin.userTabs.*;
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
    StatusForPd statusForPd;
    ViewingHistory viewingHistory;
    ViewingHistoryForPd viewingHistoryForPd;
    PaymentHistory paymentHistory;
    GiftHistory giftHistory;
    GiftHistoryForPd giftHistoryForPd;
    WithdrawHistoryForPd withdrawHistoryForPd;
    TreeSummary treeSummary;
    TreeSummaryGridResult treeSummaryGridResult;
    RealTimeTreeTransactionHistory realTimeTreeTransactionHistory;
    RealTimeLeafTransactionHistory realTimeLeafTransactionHistory;
    TotalTreeUsageSummary totalTreeUsageSummary;
    TotalLeavesUsageSummary totalLeavesUsageSummary;

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, Status status) {
        this.errorResponse = errorResponse;
        this.status = status;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, StatusForPd statusForPd) {
        this.errorResponse = errorResponse;
        this.statusForPd = statusForPd;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, ViewingHistory viewingHistory) {
        this.errorResponse = errorResponse;
        this.viewingHistory = viewingHistory;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, ViewingHistoryForPd viewingHistoryForPd) {
        this.errorResponse = errorResponse;
        this.viewingHistoryForPd = viewingHistoryForPd;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, PaymentHistory paymentHistory) {
        this.errorResponse = errorResponse;
        this.paymentHistory = paymentHistory;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, GiftHistory giftHistory) {
        this.errorResponse = errorResponse;
        this.giftHistory = giftHistory;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, GiftHistoryForPd giftHistoryForPd) {
        this.errorResponse = errorResponse;
        this.giftHistoryForPd = giftHistoryForPd;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, WithdrawHistoryForPd withdrawHistoryForPd) {
        this.errorResponse = errorResponse;
        this.withdrawHistoryForPd = withdrawHistoryForPd;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, TreeSummaryGridResult treeSummaryGridResult) {
        this.errorResponse = errorResponse;
        this.treeSummaryGridResult = treeSummaryGridResult;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, TreeSummary treesSummaryTotals) {
        this.errorResponse = errorResponse;
        this.treeSummary = treesSummaryTotals;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, RealTimeTreeTransactionHistory realTimeTreeTransactionHistory) {
        this.errorResponse = errorResponse;
        this.realTimeTreeTransactionHistory = realTimeTreeTransactionHistory;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, TotalTreeUsageSummary totalTreeUsageSummary) {
        this.errorResponse = errorResponse;
        this.totalTreeUsageSummary = totalTreeUsageSummary;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, TotalLeavesUsageSummary totalLeavesUsageSummary) {
        this.errorResponse = errorResponse;
        this.totalLeavesUsageSummary = totalLeavesUsageSummary;
    }

    public AdminDashboardUserPaymentStats(ErrorResponse errorResponse, RealTimeLeafTransactionHistory realTimeLeafTransactionHistory) {
        this.errorResponse = errorResponse;
        this.realTimeLeafTransactionHistory = realTimeLeafTransactionHistory;
    }

}
