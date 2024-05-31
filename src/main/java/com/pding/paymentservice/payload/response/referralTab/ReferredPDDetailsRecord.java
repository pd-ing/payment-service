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
    private BigDecimal totalTreesInEarningWallet;
    private String registrationDate;
    private String referenceCode;
    private String referredPdGrade;

    public static ReferredPDDetailsRecord fromObjectArray(Object[] objectArr) {
        ReferredPDDetailsRecord referredPDDetailsRecord = new ReferredPDDetailsRecord();

        referredPDDetailsRecord.setReferredPdNickname(objectArr[0].toString());
        referredPDDetailsRecord.setReferredPdEmail(objectArr[1].toString());
        referredPDDetailsRecord.setTotalTreesInEarningWallet((BigDecimal) objectArr[2]);
        referredPDDetailsRecord.setRegistrationDate(objectArr[3].toString());
        referredPDDetailsRecord.setReferenceCode(objectArr[4].toString());
        referredPDDetailsRecord.setReferredPdGrade(objectArr[5].toString());

        return referredPDDetailsRecord;
    }
}




