package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserObject {
    private String pdUserId;
    private String nickName;
    private String email;
    private String pdType;
    private TreeSummary treeSummary;
}
