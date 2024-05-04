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


    public static ReferredPdDetailsDTO fromObjectArray(Object[] objects) {
        return new ReferredPdDetailsDTO(
                (String) objects[0],
                (String) objects[1],
                (String) objects[2],
                (String) objects[3],
                (String) objects[4],
                (String) objects[5],
                (String) objects[6]
        );
    }
}
