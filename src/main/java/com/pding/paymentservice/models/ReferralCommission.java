package com.pding.paymentservice.models;

import com.pding.paymentservice.models.enums.CommissionTransferStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "ReferralCommission")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCommission {
    @Id
    @UuidGenerator
    private String id;

    @Column(name = "withdrawal_id")
    private String withdrawalId;

    @Column(name = "referrer_user_id")
    private String referrerUserId;

    @Column(name = "referred_user_id")
    private String referredUserId;

    @Column(name = "commission_percent")
    private String commissionPercent;

    @Column(name = "commission_amount")
    private String commissionAmountInTrees;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Enumerated(EnumType.STRING)
    private CommissionTransferStatus commissionTransferStatus;

    // Payment Information
    @Column(name = "transfer_id")
    private String transferId;

    @Column(name = "transfer_amount")
    private BigDecimal transferAmount;

    @Column(name = "transfer_description")
    private String transferDescription;

    public ReferralCommission(String withdrawalId, String referrerUserId, String referredUserId, String commissionPercent, String commissionAmountInTrees,
                              LocalDateTime createdDate, LocalDateTime updatedDate, CommissionTransferStatus commissionTransferStatus, String transferId, BigDecimal transferAmount,
                              String transferDescription) {
        this.withdrawalId = withdrawalId;
        this.referrerUserId = referrerUserId;
        this.referredUserId = referrerUserId;
        this.commissionPercent = commissionPercent;
        this.commissionAmountInTrees = commissionAmountInTrees;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.commissionTransferStatus = commissionTransferStatus;
        this.transferId = transferId;
        this.transferAmount = transferAmount;
        this.transferDescription = transferDescription;
    }
}
