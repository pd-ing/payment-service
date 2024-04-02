package com.pding.paymentservice.payload.response.TreeSpentHistory;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TreeSpentHistoryResponse {
    ErrorResponse errorResponse;
    BigDecimal totalTreesSpent;
    Page<TreeSpentHistoryRecord> treeSpentHistoryRecord;
}
