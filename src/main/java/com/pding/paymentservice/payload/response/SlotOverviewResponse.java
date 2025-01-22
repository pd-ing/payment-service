package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotOverviewResponse {
    private String userId;
    private String slotId;
    private String nickname;
    private String ticket_type;
    private Instant startTime;
    private Instant endTime;
}
