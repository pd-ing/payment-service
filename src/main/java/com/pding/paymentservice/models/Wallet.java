package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@NoArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userID;

    private BigDecimal trees;


    private String stripeCustomerID;
    private LocalDateTime lastPurchasedDate;


    public Wallet(Long userID, String stripeCustomerID, BigDecimal trees, LocalDateTime lastPurchasedDate) {
        this.userID = userID;
        this.stripeCustomerID = stripeCustomerID;
        this.trees = trees;
        this.lastPurchasedDate = lastPurchasedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getUser() {
        return userID;
    }

    public void setUser(Long userID) {
        this.userID = userID;
    }

    public BigDecimal getTrees() {
        return trees;
    }

    public void setTrees(BigDecimal trees) {
        this.trees = trees;
    }

    public LocalDateTime getLastPurchasedDate() {
        return lastPurchasedDate;
    }

    public void setLastPurchasedDate(LocalDateTime lastPurchasedDate) {
        this.lastPurchasedDate = lastPurchasedDate;
    }

    public String getStripeCustomerID() {
        return stripeCustomerID;
    }

    public void setStripeCustomerID(String stripeCustomerID) {
        this.stripeCustomerID = stripeCustomerID;
    }
}