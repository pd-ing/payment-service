package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private String id;

    private String walletOrVideoOrDonationOrWithdrawalId;

    private BigDecimal treesTransacted;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // "Purchase", "Donation", "Withdrawal", etc.

    public Ledger(String walletOrVideoOrDonationOrWithdrawalId, BigDecimal treesTransacted, TransactionType transactionType) {
        this.walletOrVideoOrDonationOrWithdrawalId = walletOrVideoOrDonationOrWithdrawalId;
        this.treesTransacted = treesTransacted;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();

    }
}


