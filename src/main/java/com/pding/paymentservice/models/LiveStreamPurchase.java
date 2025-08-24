package com.pding.paymentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "livestream_purchases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveStreamPurchase {
    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String buyerUserId; // May add the relation to the User entity to retrieve the livestream payment history

    @Column(nullable = false)
    private String pdUserId;

    @Column(nullable = false)
    private String livestreamId;

    @Column(nullable = false)
    private BigDecimal treesOffered;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;
}