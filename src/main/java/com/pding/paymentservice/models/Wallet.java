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
@Table(name = "wallet")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private BigDecimal trees;

    private LocalDateTime lastPurchasedDate;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Boolean isActive = true;

    private Integer totalTransactions = 0;

    public Wallet(String userId, BigDecimal trees, LocalDateTime lastPurchasedDate) {
        this.userId = userId;
        this.trees = trees;
        this.lastPurchasedDate = lastPurchasedDate;
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        totalTransactions = 1;
    }
}