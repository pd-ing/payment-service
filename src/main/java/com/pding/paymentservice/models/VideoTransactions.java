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

    private long userID;

    private long contentID;

    private BigDecimal treesConsumed;

    private LocalDateTime timestamp;

    public VideoTransactions(long userID, long contentID, BigDecimal treesConsumed) {
        this.userID = userID;
        this.contentID = contentID;
        this.treesConsumed = treesConsumed;
        this.timestamp = LocalDateTime.now();
    }

}
