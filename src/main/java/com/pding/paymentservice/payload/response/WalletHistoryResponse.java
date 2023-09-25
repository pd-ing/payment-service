package com.pding.paymentservice.payload.response;

import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.models.WalletHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WalletHistoryResponse {
    ErrorResponse errorResponse;
    List<WalletHistory> walletHistory;
}
