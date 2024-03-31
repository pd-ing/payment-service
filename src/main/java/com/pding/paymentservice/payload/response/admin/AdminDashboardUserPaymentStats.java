package com.pding.paymentservice.payload.response.admin;


import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.admin.userTabs.PaymentHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.Status;
import com.pding.paymentservice.payload.response.admin.userTabs.ViewingHistory;
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
}
