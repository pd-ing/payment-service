package com.pding.paymentservice.payload.response.referralTab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferredPDWithdrawalRecord {

    private String referredPdNickname;
    private String referredPdEmail;
    private String registrationDate;
    private String referredPdGrade;
    private BigDecimal totalTreesInEarningWallet;
    private String lastWithdrawRequest;
    private BigDecimal exchangedTrees;

}




