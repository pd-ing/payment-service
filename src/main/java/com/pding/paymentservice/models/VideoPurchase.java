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
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "VideoPurchase")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
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

    public VideoPurchase(String userId, String videoId, BigDecimal treesConsumed, String videoOwnerUserId) {
        this.userId = userId;
        this.videoId = videoId;
        this.treesConsumed = treesConsumed;
        this.lastUpdateDate = LocalDateTime.now();
        this.videoOwnerUserId = videoOwnerUserId;
    }

}
