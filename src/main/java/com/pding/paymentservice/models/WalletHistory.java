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
@Table(name = "wallethistory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long walletId;

    private Long userId;

    private BigDecimal purchasedTrees;

    private LocalDateTime purchaseDate;

    private String transactionId;

    private String transactionStatus;

    private BigDecimal amount;

    private String paymentMethod;

    private String currency;

    private String description;

    private String ipAddress;

    public WalletHistory(Long walletId, Long userId, BigDecimal purchasedTrees,
                         LocalDateTime purchaseDate, String transactionId, String transactionStatus,
                         BigDecimal amount, String paymentMethod, String currency, String description,
                         String ipAddress) {
        this.walletId = walletId;
        this.userId = userId;
        this.purchasedTrees = purchasedTrees;
        this.purchaseDate = purchaseDate;
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.currency = currency;
        this.description = description;
        this.ipAddress = ipAddress;
    }
}