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

    private Long userID;

    private String videoID;

    private BigDecimal treesConsumed;

    private LocalDateTime timestamp;

    private Long videoOwnerUserID;

    public VideoTransactions(Long userID, String videoID, BigDecimal treesConsumed, Long videoOwnerUserID) {
        this.userID = userID;
        this.videoID = videoID;
        this.treesConsumed = treesConsumed;
        this.timestamp = LocalDateTime.now();
        this.videoOwnerUserID = videoOwnerUserID;
    }

}
