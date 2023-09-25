package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallethistory")
public class WalletHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long walletId;


    private Long userId;

    private String stripeCustomerID;
    private BigDecimal purchasedTrees; // Store the number of custom currency units purchased

    private LocalDateTime purchaseDate; // Date of the purchase

    public WalletHistory() {
    }

    public WalletHistory(Long userId, String stripeCustomerID, Long walletId, BigDecimal purchasedTrees, LocalDateTime purchaseDate) {
        this.userId = userId;
        this.stripeCustomerID = stripeCustomerID;
        this.walletId = walletId;
        this.purchasedTrees = purchasedTrees;
        this.purchaseDate = purchaseDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getPurchasedTrees() {
        return purchasedTrees;
    }

    public void setPurchasedTrees(BigDecimal purchasedTrees) {
        this.purchasedTrees = purchasedTrees;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStripeCustomerID() {
        return stripeCustomerID;
    }

    public void setStripeCustomerID(String stripeCustomerID) {
        this.stripeCustomerID = stripeCustomerID;
    }
}