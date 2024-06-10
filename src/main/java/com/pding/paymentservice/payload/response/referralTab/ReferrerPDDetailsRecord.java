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
public class ReferrerPDDetailsRecord {
    private String referrerPdUserId;
    private String referrerPdNickname;
    private String referrerPdEmail;
    private BigDecimal totalTreesInEarningWalletOfReferrer;
    private String registrationDate;
    private String referenceCode;
    private String referrerPdGrade;
    private BigDecimal sumOfTreesEarnedByReferredPDs;
    private BigDecimal sumOfLeafsEarnedByReferredPDs;

    public static ReferrerPDDetailsRecord fromObjectArray(Object[] objectArr) {
        ReferrerPDDetailsRecord referrerPDDetailsRecord = new ReferrerPDDetailsRecord();

        referrerPDDetailsRecord.setReferrerPdUserId(objectArr[0].toString());
        referrerPDDetailsRecord.setReferrerPdNickname(objectArr[1].toString());
        referrerPDDetailsRecord.setReferrerPdEmail(objectArr[2].toString());
        referrerPDDetailsRecord.setTotalTreesInEarningWalletOfReferrer((BigDecimal) objectArr[3]);
        referrerPDDetailsRecord.setRegistrationDate(objectArr[4].toString());
        referrerPDDetailsRecord.setReferenceCode(objectArr[5].toString());
        referrerPDDetailsRecord.setReferrerPdGrade(objectArr[6].toString());

        return referrerPDDetailsRecord;
    }
}
