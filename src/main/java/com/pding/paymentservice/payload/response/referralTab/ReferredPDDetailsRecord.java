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
public class ReferredPDDetailsRecord {

    private String referredPdNickname;
    private String referredPdEmail;
    private String registrationDate;
    private String referredPdGrade;
    private BigDecimal totalTreesInEarningWallet;
    private String lastWithdrawRequest;
    private BigDecimal exchangedTrees;

    public static ReferredPDDetailsRecord fromObjectArray(Object[] objectArr) {
        ReferredPDDetailsRecord referredPDDetailsRecord = new ReferredPDDetailsRecord();

        referredPDDetailsRecord.setReferredPdNickname(objectArr[0].toString());
        referredPDDetailsRecord.setReferredPdEmail(objectArr[1].toString());
        referredPDDetailsRecord.setRegistrationDate(objectArr[2].toString());
        referredPDDetailsRecord.setReferredPdGrade(objectArr[3].toString());
        referredPDDetailsRecord.setTotalTreesInEarningWallet((BigDecimal) objectArr[4]);
        referredPDDetailsRecord.setLastWithdrawRequest(objectArr[5].toString());
        referredPDDetailsRecord.setExchangedTrees((BigDecimal) objectArr[6]);


        return referredPDDetailsRecord;
    }
}




