package com.pding.paymentservice.payload.response.Pagination;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse {
    ErrorResponse errorResponse;
    PaginationInfoWithGenericList paginationInfoWithGenericList;
}
