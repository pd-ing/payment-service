package com.pding.paymentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "Earning")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Earning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    private BigDecimal treesEarned;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Integer totalTransactions = 0;

    public Earning(String userId, BigDecimal treesEarned) {
        LocalDateTime currentDate = LocalDateTime.now();
        this.userId = userId;
        this.treesEarned = treesEarned;
        this.createdDate = currentDate;
        this.updatedDate = currentDate;
        this.totalTransactions = 1;
    }
}
