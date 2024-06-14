package com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferredPdDetails {
    String pdUserId;
    String email;
    String nickname;
    String treesEarned;
    String leavesEarned;
    String treesWithdrawn;
    String leavesWithdrawn;

    public static ReferredPdDetails fromObjectArray(Object[] objectArr) {
        ReferredPdDetails referredPdDetails = new ReferredPdDetails();

        referredPdDetails.setPdUserId(objectArr[0].toString());
        referredPdDetails.setEmail(objectArr[1].toString());
        referredPdDetails.setNickname(objectArr[2].toString());
        referredPdDetails.setTreesEarned(objectArr[3].toString());
        referredPdDetails.setTreesWithdrawn(objectArr[4].toString());
        referredPdDetails.setLeavesWithdrawn(objectArr[5].toString());
        referredPdDetails.setLeavesEarned(objectArr[6].toString());
        return referredPdDetails;
    }
}
