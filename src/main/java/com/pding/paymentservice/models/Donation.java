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
import org.hibernate.annotations.UuidGenerator;

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
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private String id;

    private String donorUserId;

    private String pdUserId;

    private BigDecimal donatedTrees;

    private LocalDateTime lastUpdateDate;

    public Donation(String donorUserId, String pdUserId, BigDecimal donatedTrees) {
        this.donorUserId = donorUserId;
        this.pdUserId = pdUserId;
        this.donatedTrees = donatedTrees;
        this.lastUpdateDate = LocalDateTime.now();
    }
}
