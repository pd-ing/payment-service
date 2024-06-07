package com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferralCommissionHistory {
    private String referralCommissionId;
    private String withdrawalId;
    private String referrerPdUserId;
    private String referrerCommissionPercent;
    private String referrerCommissionAmountInTrees;
    private String referrerCommissionAmountInLeafs;
    private String referrerCommissionCreatedDate;
    private String referrerCommissionUpdatedDate;
    private String referrerCommissionTransferStatus;
    private String referrerUserNickname;
    private String referrerUserEmail;
    private String referrerPdType;
    private String linkedStripeId;
    private String referredPdUserId;
    private String pdAffiliated;

    public static ReferralCommissionHistory fromObjectArray(Object[] objectArr) throws Exception {

        ReferralCommissionHistory referralCommissionHistory = new ReferralCommissionHistory();

        referralCommissionHistory.setReferralCommissionId(objectArr[0].toString());
        referralCommissionHistory.setWithdrawalId(objectArr[1].toString());
        referralCommissionHistory.setReferrerPdUserId(objectArr[2].toString());
        referralCommissionHistory.setReferrerCommissionPercent(objectArr[3].toString());
        referralCommissionHistory.setReferrerCommissionAmountInTrees(objectArr[4].toString());
        referralCommissionHistory.setReferrerCommissionAmountInLeafs(objectArr[5].toString());
        referralCommissionHistory.setReferrerCommissionCreatedDate(objectArr[6].toString());
        referralCommissionHistory.setReferrerCommissionUpdatedDate(objectArr[7].toString());
        referralCommissionHistory.setReferrerCommissionTransferStatus(objectArr[8].toString());
        referralCommissionHistory.setReferrerUserNickname(objectArr[9].toString());
        referralCommissionHistory.setReferrerUserEmail(objectArr[10].toString());
        referralCommissionHistory.setReferrerPdType(objectArr[11].toString());
        referralCommissionHistory.setLinkedStripeId(objectArr[12].toString());
        referralCommissionHistory.setReferredPdUserId(objectArr[13].toString());

        return referralCommissionHistory;
    }
}
