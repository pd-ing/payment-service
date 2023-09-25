package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "VideoTransactions")
public class VideoTransactions {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long userID;

    private long contentID;
    private BigDecimal treesConsumed;

    private LocalDateTime timestamp;

    public VideoTransactions() {
    }

    public VideoTransactions(long userID, long contentID, BigDecimal treesConsumed) {
        this.userID = userID;
        this.contentID = contentID;
        this.treesConsumed = treesConsumed;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public BigDecimal getTreesConsumed() {
        return treesConsumed;
    }

    public void setTreesConsumed(BigDecimal treesConsumed) {
        this.treesConsumed = treesConsumed;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getContentID() {
        return contentID;
    }

    public void setContentID(long contentID) {
        this.contentID = contentID;
    }
}
