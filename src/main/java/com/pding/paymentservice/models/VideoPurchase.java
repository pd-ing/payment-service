package com.pding.paymentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "VideoPurchase")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
public class VideoPurchase {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private String id;

    private String userId;

    private String videoId;

    private BigDecimal treesConsumed;

    private LocalDateTime lastUpdateDate;

    private String videoOwnerUserId;

    private Boolean isReplacementOfDeletedVideo;

    private String duration;
    private LocalDateTime expiryDate;

    @Column(columnDefinition = "boolean default false")
    private Boolean isRefunded = false;

    private BigDecimal drmFee;

    private String packagePurchaseId;

    private Integer discountPercentageApplied;

    public VideoPurchase(String userId, String videoId, BigDecimal treesConsumed, String videoOwnerUserId) {
        this.userId = userId;
        this.videoId = videoId;
        this.treesConsumed = treesConsumed;
        this.lastUpdateDate = LocalDateTime.now();
        this.videoOwnerUserId = videoOwnerUserId;
        this.isReplacementOfDeletedVideo = false;
        this.duration = "PERMANENT";
        this.isRefunded = false;
    }

    public VideoPurchase(String userId, String videoId, BigDecimal treesConsumed, String videoOwnerUserId, String duration, LocalDateTime expiryDate, BigDecimal drmFee) {
        this.userId = userId;
        this.videoId = videoId;
        this.treesConsumed = treesConsumed;
        this.lastUpdateDate = LocalDateTime.now();
        this.videoOwnerUserId = videoOwnerUserId;
        this.isReplacementOfDeletedVideo = false;
        this.duration = duration;
        this.expiryDate = expiryDate;
        this.isRefunded = false;
        this.drmFee = drmFee;
    }

    public VideoPurchase(String userId, String videoId, BigDecimal treesConsumed, String videoOwnerUserId, Boolean isReplacementOfDeletedVideo) {
        this.userId = userId;
        this.videoId = videoId;
        this.treesConsumed = treesConsumed;
        this.lastUpdateDate = LocalDateTime.now();
        this.videoOwnerUserId = videoOwnerUserId;
        this.isReplacementOfDeletedVideo = isReplacementOfDeletedVideo;
        this.isRefunded = false;
    }

    public VideoPurchase(String userId, String videoId, BigDecimal treesConsumed, String videoOwnerUserId, String duration, LocalDateTime expiryDate, Integer discountPercentageApplied, String packagePurchaseId, BigDecimal drmFee) {
        this.userId = userId;
        this.videoId = videoId;
        this.treesConsumed = treesConsumed;
        this.lastUpdateDate = LocalDateTime.now();
        this.videoOwnerUserId = videoOwnerUserId;
        this.isReplacementOfDeletedVideo = false;
        this.duration = duration;
        this.expiryDate = expiryDate;
        this.isRefunded = false;
        this.packagePurchaseId = packagePurchaseId;
        this.discountPercentageApplied = discountPercentageApplied;
        this.drmFee = drmFee;
    }

}
