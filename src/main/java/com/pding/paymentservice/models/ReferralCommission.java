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

    @Column(name = "referrer_pd_user_id")
    private String referrerPdUserId;

    @Column(name = "commission_percent")
    private String commissionPercent;

    @Column(name = "commission_amount_in_trees")
    private String commissionAmountInTrees = new BigDecimal(0).toString();

    @Column(name = "commission_amount_in_leafs")
    private String commissionAmountInLeafs = new BigDecimal(0).toString();

    @Column(name = "commission_amount_in_cents")
    private BigDecimal commissionAmountInCents;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Enumerated(EnumType.STRING)
    private CommissionTransferStatus commissionTransferStatus;


    public ReferralCommission(String withdrawalId,
                              String referrerPdUserId,
                              String commissionPercent,
                              String commissionAmountInTrees,
                              String commissionAmountInLeafs,
                              LocalDateTime createdDate,
                              LocalDateTime updatedDate,
                              CommissionTransferStatus commissionTransferStatus,
                              BigDecimal commissionAmountInCents
    ) {
        this.withdrawalId = withdrawalId;
        this.referrerPdUserId = referrerPdUserId;
        this.commissionPercent = commissionPercent;
        this.commissionAmountInTrees = commissionAmountInTrees;
        this.commissionAmountInLeafs = commissionAmountInLeafs;
        this.commissionAmountInCents = commissionAmountInCents;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.commissionTransferStatus = commissionTransferStatus;

    }
}
