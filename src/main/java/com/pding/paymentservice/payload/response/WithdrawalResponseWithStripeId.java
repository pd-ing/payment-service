package com.pding.paymentservice.payload.response;

import com.pding.paymentservice.models.enums.WithdrawalStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalResponseWithStripeId {
    private String id;

    private String pdUserId;

    private BigDecimal trees;

    private BigDecimal leafs;

    @Enumerated(EnumType.STRING)
    private WithdrawalStatus status;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    public String email;

    public String nickname;

    public String linkedStripeId;

    public String profilePicture;
}
