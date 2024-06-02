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
    private String referralPdGrade;
    private BigDecimal totalTreesInEarningWallet;
    private String lastWithdrawRequest;
    private BigDecimal exchangedTrees;

    public static ReferredPDWithdrawalRecord fromObjectArray(Object[] objectArr) {
        ReferredPDWithdrawalRecord referredPDWithdrawalRecord = new ReferredPDWithdrawalRecord();

        referredPDWithdrawalRecord.setReferredPdNickname(objectArr[0].toString());
        referredPDWithdrawalRecord.setReferredPdEmail(objectArr[1].toString());
        referredPDWithdrawalRecord.setTotalTreesInEarningWallet((BigDecimal) objectArr[4]);
        referredPDWithdrawalRecord.setRegistrationDate(objectArr[2].toString());
        referredPDWithdrawalRecord.setReferredPdGrade(objectArr[3].toString());
        referredPDWithdrawalRecord.setLastWithdrawRequest(objectArr[5].toString());
        referredPDWithdrawalRecord.setExchangedTrees((BigDecimal) objectArr[6]);

        return referredPDWithdrawalRecord;
    }

}




