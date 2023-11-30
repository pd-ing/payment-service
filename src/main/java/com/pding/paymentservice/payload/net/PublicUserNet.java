package com.pding.paymentservice.payload.net;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PublicUserNet {

    private String id;

    private String email;

    private Boolean isCreator;

    private String profilePicture;

    private Boolean isEnabled;

    private String nickname;

    private String description;

    private String coverImage;

    private BigDecimal treesDonated;
}
