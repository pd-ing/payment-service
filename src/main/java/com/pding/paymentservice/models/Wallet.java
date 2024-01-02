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
@Table(name = "Wallet")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Wallet {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private String id;

    @Column(unique = true)
    private String userId;

    private BigDecimal trees;

    private BigDecimal leafs;

    private LocalDateTime lastPurchasedDate;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Boolean isActive = true;

    private Integer totalTransactions = 0;

    public Wallet(String userId, BigDecimal trees, BigDecimal leafs, LocalDateTime lastPurchasedDate) {
        this.userId = userId;
        this.trees = trees;
        this.leafs = leafs;
        this.lastPurchasedDate = lastPurchasedDate;
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        totalTransactions = 1;
    }
}