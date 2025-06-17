package com.pding.paymentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Entity representing a photo purchase for web
 */
@Entity
@Table(name = "photo_purchase")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
public class PhotoPurchase {
    @Id
    @UuidGenerator
    private String id;

    private String userId;

    private String postId;

    private BigDecimal treesConsumed;

    private LocalDateTime lastUpdateDate;

    private String postOwnerUserId;

    private String duration;

    private Instant expiryDate;

    @Column(columnDefinition = "boolean default false")
    private Boolean isRefunded = false;

    public PhotoPurchase(String userId, String postId, BigDecimal treesConsumed, String postOwnerUserId) {
        this.userId = userId;
        this.postId = postId;
        this.treesConsumed = treesConsumed;
        this.lastUpdateDate = LocalDateTime.now();
        this.postOwnerUserId = postOwnerUserId;
        this.isRefunded = false;
    }

    public PhotoPurchase(String userId, String postId, BigDecimal treesConsumed, String postOwnerUserId, String duration, Instant expiryDate) {
        this.userId = userId;
        this.postId = postId;
        this.treesConsumed = treesConsumed;
        this.lastUpdateDate = LocalDateTime.now();
        this.postOwnerUserId = postOwnerUserId;
        this.duration = duration;
        this.expiryDate = expiryDate;
        this.isRefunded = false;
    }
}
