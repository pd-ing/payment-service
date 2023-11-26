package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Donation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String donorUserId;

    private String PdUserId;

    private BigDecimal donatedTrees;

    private LocalDateTime lastUpdateDate;

    public Donation(String donorUserId, String PdUserId, BigDecimal donatedTrees) {
        this.donorUserId = donorUserId;
        this.PdUserId = PdUserId;
        this.donatedTrees = donatedTrees;
        this.lastUpdateDate = LocalDateTime.now();
    }
}
