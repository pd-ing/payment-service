package com.pding.paymentservice.payload.response.generic;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenericPageResponse<T> {
    private ErrorResponse errorResponse;
    private Page<T> data;
}
