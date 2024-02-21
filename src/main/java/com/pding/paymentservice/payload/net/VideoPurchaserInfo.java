package com.pding.paymentservice.payload.net;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VideoPurchaserInfo {

    private String email;

    private String userId;

    private String profilePicture;

    private String date;

}
