package com.pding.paymentservice.payload.response.admin.userTabs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalLeavesUsageSummary {

    BigDecimal totalLeavesTransacted;
    BigDecimal totalLeavesUsedForVideoCall;
    BigDecimal totalLeavesUsedForVoiceCall;
    BigDecimal totalLeavesUsedForChat;
    BigDecimal totalLeavesUsedForGift;
    BigDecimal totalLeavesUsedForInChatMediaBuying;
    BigDecimal totalLeavesDonated;
}