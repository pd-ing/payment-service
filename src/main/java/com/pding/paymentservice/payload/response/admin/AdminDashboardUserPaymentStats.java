package com.pding.paymentservice.payload.response.admin;


import com.pding.paymentservice.payload.response.ErrorResponse;
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
}
