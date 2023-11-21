package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "VideoTransactions")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class VideoTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String videoId;

    private BigDecimal treesConsumed;

    private LocalDateTime timestamp;

    private String videoOwnerUserId;

    public VideoTransactions(String userId, String videoId, BigDecimal treesConsumed, String videoOwnerUserId) {
        this.userId = userId;
        this.videoId = videoId;
        this.treesConsumed = treesConsumed;
        this.timestamp = LocalDateTime.now();
        this.videoOwnerUserId = videoOwnerUserId;
    }

}
