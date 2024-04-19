package com.pding.paymentservice.payload.response.admin;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.UserObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TreeSummaryGridResult {
    Page<UserObject> userObjects;
}
