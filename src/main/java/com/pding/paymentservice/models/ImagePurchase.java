package com.pding.paymentservice.models;

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
import java.time.LocalDateTime;

@Entity
@Table(name = "ImagePurchase")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
public class ImagePurchase {
    @Id
    @UuidGenerator
    private String id;

    private String userId;

    private String postId;

    private BigDecimal leafAmount;

    private LocalDateTime lastUpdateDate;

    private String postOwnerUserId;

    public ImagePurchase(String userId, String postId, BigDecimal leafAmount, String postOwnerUserId) {
        this.userId = userId;
        this.postId = postId;
        this.leafAmount = leafAmount;
        this.lastUpdateDate = LocalDateTime.now();
        this.postOwnerUserId = postOwnerUserId;
    }

}
