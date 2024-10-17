package com.pding.paymentservice.payload.response.generic;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenericSliceResponse<T> {
    private ErrorResponse errorResponse;
    private List<T> data;
    private Boolean hasNext;
}
