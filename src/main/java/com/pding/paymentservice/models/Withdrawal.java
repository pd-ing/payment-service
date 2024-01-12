package com.pding.paymentservice.models;

import com.pding.paymentservice.models.enums.WithdrawalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawals")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Withdrawal {

    @Id
    @UuidGenerator
    private String id;

    private String pdUserId;

    private BigDecimal trees;

    private BigDecimal leafs;

    @Enumerated(EnumType.STRING)
    private WithdrawalStatus status;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    public Withdrawal(String pdUerId, BigDecimal trees, BigDecimal leafs, WithdrawalStatus status) {
        this.pdUserId = pdUerId;
        this.trees = trees;
        this.leafs = leafs;
        this.status = status;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
}
