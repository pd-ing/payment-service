package com.pding.paymentservice.models;

import com.pding.paymentservice.models.enums.TransactionType;
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
@Table(name = "Ledger")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ledger {
    @Id
    @UuidGenerator
    private String id;

    private String walletOrVideoOrDonationOrWithdrawalId; // This is EOL'd on 5 Jan,2024

    private String walletId;

    private String videoId;

    private String withdrawId;

    private String donationId;

    private String callId;

    private BigDecimal treesTransacted;

    private BigDecimal leafsTransacted;

    private LocalDateTime timestamp;

    private String transactionId;
    private String packageId;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // "Purchase", "Donation", "Withdrawal", etc.

    private String userId;

    // referenceId can be walletId, videoId, withdrawId, donationId, earningId
    public Ledger(String referenceId, BigDecimal treesTransacted, BigDecimal leafsTransacted, TransactionType transactionType, String userId) {
        if (transactionType.equals(TransactionType.TREE_PURCHASE) || transactionType.equals(TransactionType.LEAF_PURCHASE)) {
            this.walletId = referenceId;
        } else if (transactionType.equals(TransactionType.PAYMENT_COMPLETED) || transactionType.equals(TransactionType.PAYMENT_FAILED) || transactionType.equals(TransactionType.REFUND_COMPLETED)) {
            this.walletId = referenceId;
        } else if (transactionType.equals(TransactionType.VIDEO_PURCHASE)) {
            this.videoId = referenceId;
        } else if (transactionType.equals(TransactionType.WITHDRAWAL_STARTED) || transactionType.equals(TransactionType.WITHDRAWAL_FAILED) ||
                transactionType.equals(TransactionType.WITHDRAWAL_COMPLETED) || transactionType.equals(TransactionType.TREES_REVERTED)) {
            this.withdrawId = referenceId;
        } else if (transactionType.equals(TransactionType.DONATION)) {
            this.donationId = referenceId;
        } else if (transactionType.equals(TransactionType.AUDIO_CALL) || transactionType.equals(TransactionType.VIDEO_CALL) || transactionType.equals(TransactionType.TEXT_MESSAGE)) {
            this.callId = referenceId;
        } else if (transactionType.equals(TransactionType.PACKAGE_PURCHASE) || transactionType.equals(TransactionType.REFUND_PACKAGE_PURCHASE)) {
            this.packageId = referenceId;
        }
        this.transactionId = referenceId;
        this.treesTransacted = treesTransacted;
        this.leafsTransacted = leafsTransacted;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
    }
}


