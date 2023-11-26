package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long walletOrVideoOrDonationOrWithdrawalId;

    private BigDecimal treesTransacted;

    private LocalDateTime timestamp;

    private TransactionType transactionType; // "Purchase", "Donation", "Withdrawal", etc.

    public Ledger(Long walletOrVideoOrDonationOrWithdrawalId, BigDecimal treesTransacted, TransactionType transactionType) {
        this.walletOrVideoOrDonationOrWithdrawalId = walletOrVideoOrDonationOrWithdrawalId;
        this.treesTransacted = treesTransacted;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();

    }
}


