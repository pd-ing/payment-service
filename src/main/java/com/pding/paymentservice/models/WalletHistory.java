package com.pding.paymentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "WalletHistory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletHistory {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private String id;

    private String walletId;

    private String userId;

    private BigDecimal purchasedTrees;

    private BigDecimal purchasedLeafs;

    private LocalDateTime purchaseDate;

    @Column(unique = true)
    private String transactionId;

    private String transactionStatus;

    private BigDecimal amount;

    private String paymentMethod;

    private String currency;

    private String description;

    private String ipAddress;

    private String refundId;

    private Boolean isFirstPurchase;

    public WalletHistory(String walletId, String userId, BigDecimal purchasedTrees, BigDecimal purchasedLeafs,
                         LocalDateTime purchaseDate, String transactionId, String transactionStatus,
                         BigDecimal amount, String paymentMethod, String currency, String description,
                         String ipAddress) {
        this.walletId = walletId;
        this.userId = userId;
        this.purchasedTrees = purchasedTrees;
        this.purchasedLeafs = purchasedLeafs;
        this.purchaseDate = purchaseDate;
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.currency = currency;
        this.description = description;
        this.ipAddress = ipAddress;
    }

    public WalletHistory(String walletId, String userId, BigDecimal purchasedTrees, BigDecimal purchasedLeafs,
                         LocalDateTime purchaseDate, String transactionId, String transactionStatus,
                         BigDecimal amount, String paymentMethod, String currency, String description,
                         String ipAddress, String refundId) {
        this.walletId = walletId;
        this.userId = userId;
        this.purchasedTrees = purchasedTrees;
        this.purchasedLeafs = purchasedLeafs;
        this.purchaseDate = purchaseDate;
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.currency = currency;
        this.description = description;
        this.ipAddress = ipAddress;
        this.refundId = refundId;
    }
}
