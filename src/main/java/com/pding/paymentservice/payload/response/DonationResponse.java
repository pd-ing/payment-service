package com.pding.paymentservice.payload.response;

import com.pding.paymentservice.models.Donation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DonationResponse {
    ErrorResponse errorResponse;
    Donation donation;
}
