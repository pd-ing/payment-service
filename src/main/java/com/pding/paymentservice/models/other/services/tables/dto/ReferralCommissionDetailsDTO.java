package com.pding.paymentservice.models.other.services.tables.dto;

import com.pding.paymentservice.models.enums.CommissionTransferStatus;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

        referralCommissionDetailsDTOObj.setReferralCommissionId(objectArr[0].toString());
        referralCommissionDetailsDTOObj.setWithdrawalId(objectArr[1].toString());
        referralCommissionDetailsDTOObj.setReferrerPdUserId(objectArr[2].toString());
        referralCommissionDetailsDTOObj.setCommissionPercent(objectArr[3].toString());
        referralCommissionDetailsDTOObj.setCommissionAmountInTrees(objectArr[4].toString());
        referralCommissionDetailsDTOObj.setCommissionAmountInCents(objectArr[5].toString());
        referralCommissionDetailsDTOObj.setReferralCommissionCreatedDate(objectArr[6].toString());
        referralCommissionDetailsDTOObj.setReferralCommissionUpdatedDate(objectArr[7].toString());
        referralCommissionDetailsDTOObj.setCommissionTransferStatus(objectArr[8].toString());
        referralCommissionDetailsDTOObj.setWithdrawalUserId(objectArr[9].toString());
        referralCommissionDetailsDTOObj.setWithdrawalTrees(objectArr[10].toString());
        referralCommissionDetailsDTOObj.setWithdrawalLeafs(objectArr[11].toString());
        referralCommissionDetailsDTOObj.setWithdrawalStatus(objectArr[12].toString());
        referralCommissionDetailsDTOObj.setWithdrawalCreatedDate(objectArr[13].toString());
        referralCommissionDetailsDTOObj.setWithdrawalUpdatedDate(objectArr[14].toString());
        referralCommissionDetailsDTOObj.setUserNickname(objectArr[15].toString());
        referralCommissionDetailsDTOObj.setPdType(objectArr[16].toString());

        return referralCommissionDetailsDTOObj;
    }
}
