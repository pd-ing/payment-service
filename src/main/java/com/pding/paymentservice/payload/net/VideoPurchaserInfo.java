package com.pding.paymentservice.payload.net;

import lombok.*;

import java.time.LocalDateTime;

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

    private String duration;

    private LocalDateTime expiryDate;

}
