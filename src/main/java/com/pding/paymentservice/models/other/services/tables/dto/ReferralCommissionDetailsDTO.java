package com.pding.paymentservice.models.other.services.tables.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferralCommissionDetailsDTO {
    private String referralCommissionId;
    private String withdrawalId;
    private String referrerPdUserId;
    private String commissionPercent;
    private String commissionAmountInTrees;
    private String commissionAmountInCents;
    private String referralCommissionCreatedDate;
    private String referralCommissionUpdatedDate;
    private String commissionTransferStatus;
    private String withdrawalUserId;
    private String withdrawalTrees;
    private String withdrawalLeafs;
    private String withdrawalStatus;
    private String withdrawalCreatedDate;
    private String withdrawalUpdatedDate;
    private String userNickname;
    private String pdType;

    public static ReferralCommissionDetailsDTO fromObjectArray(Object[] objectArr) {
        ReferralCommissionDetailsDTO referralCommissionDetailsDTOObj = new ReferralCommissionDetailsDTO();

        referralCommissionDetailsDTOObj.setReferralCommissionId(objectArr[0] != null ? objectArr[0].toString() : null);
        referralCommissionDetailsDTOObj.setWithdrawalId(objectArr[1] != null ? objectArr[1].toString() : null);
        referralCommissionDetailsDTOObj.setReferrerPdUserId(objectArr[2] != null ? objectArr[2].toString() : null);
        referralCommissionDetailsDTOObj.setCommissionPercent(objectArr[3] != null ? objectArr[3].toString() : null);
        referralCommissionDetailsDTOObj.setCommissionAmountInTrees(objectArr[4] != null ? objectArr[4].toString() : null);
        referralCommissionDetailsDTOObj.setCommissionAmountInCents(objectArr[5] != null ? objectArr[5].toString() : null);
        referralCommissionDetailsDTOObj.setReferralCommissionCreatedDate(objectArr[6] != null ? objectArr[6].toString() : null);
        referralCommissionDetailsDTOObj.setReferralCommissionUpdatedDate(objectArr[7] != null ? objectArr[7].toString() : null);
        referralCommissionDetailsDTOObj.setCommissionTransferStatus(objectArr[8] != null ? objectArr[8].toString() : null);
        referralCommissionDetailsDTOObj.setWithdrawalUserId(objectArr[9] != null ? objectArr[9].toString() : null);
        referralCommissionDetailsDTOObj.setWithdrawalTrees(objectArr[10] != null ? objectArr[10].toString() : null);
        referralCommissionDetailsDTOObj.setWithdrawalLeafs(objectArr[11] != null ? objectArr[11].toString() : null);
        referralCommissionDetailsDTOObj.setWithdrawalStatus(objectArr[12] != null ? objectArr[12].toString() : null);
        referralCommissionDetailsDTOObj.setWithdrawalCreatedDate(objectArr[13] != null ? objectArr[13].toString() : null);
        referralCommissionDetailsDTOObj.setWithdrawalUpdatedDate(objectArr[14] != null ? objectArr[14].toString() : null);
        referralCommissionDetailsDTOObj.setUserNickname(objectArr[15] != null ? objectArr[15].toString() : null);
        referralCommissionDetailsDTOObj.setPdType(objectArr[16] != null ? objectArr[16].toString() : null);

        return referralCommissionDetailsDTOObj;
    }
}
