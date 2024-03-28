package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Donation")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
public class DonationHistory {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private String id;

    private String donorUserId;

    private BigDecimal treesDonated;

    private LocalDateTime lastUpdateDate;

    private String pdUserId;

    public DonationHistory(String donorUserId, BigDecimal treesDonated, String pdUserId) {
        this.donorUserId = donorUserId;
        this.treesDonated = treesDonated;
        this.lastUpdateDate = LocalDateTime.now();
        this.pdUserId = pdUserId;
    }

}
