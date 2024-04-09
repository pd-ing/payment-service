package com.pding.paymentservice.payload.response.generic;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GenericStringResponse {
    ErrorResponse errorResponse;
    String message;
}
