package com.pding.paymentservice.models.other.services.tables.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferredPdDetailsDTO {
    String nickname;

    String pdType;

    String joiningDate;

    String treesCurrentlyOwned;

    String leafsCurrentlyOwned;

    String pdUserId;

    String lastWithdrawalDate;


    public static ReferredPdDetailsDTO fromObjectArray(Object[] objectArr) {
        ReferredPdDetailsDTO referredPdDetailsDTOObj = new ReferredPdDetailsDTO();

        referredPdDetailsDTOObj.setNickname(objectArr[0].toString());
        referredPdDetailsDTOObj.setPdType(objectArr[1].toString());
        referredPdDetailsDTOObj.setJoiningDate(objectArr[2].toString());
        referredPdDetailsDTOObj.setTreesCurrentlyOwned(objectArr[3].toString());
        referredPdDetailsDTOObj.setLeafsCurrentlyOwned(objectArr[4].toString());
        referredPdDetailsDTOObj.setPdUserId(objectArr[5].toString());
        referredPdDetailsDTOObj.setLastWithdrawalDate(objectArr[6].toString());

        return referredPdDetailsDTOObj;
    }
}
