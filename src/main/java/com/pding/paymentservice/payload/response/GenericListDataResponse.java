package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GenericListDataResponse<T> {
    private ErrorResponse errorResponse;
    private List<T> data;
}
