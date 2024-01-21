package com.pding.paymentservice.payload.net;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PublicUserWithStripeIdNet {
    public String id;

    public String email;

    public String nickname;

    public String linkedStripeId;

    public String profilePicture;

    public String pdType;
}
