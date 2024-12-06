package com.pding.paymentservice.models.other.services.tables.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DonorData {
    private String donorUserId;
    private BigDecimal totalTreeDonation;
    private BigDecimal totalPurchasedVideoTree;
    private LocalDateTime lastUsedDate;
    private String lastUsedDateFormatted;
    private String email;
    private String nickname;
    private String profilePicture;
}
