package com.pding.paymentservice.models;

import com.pding.paymentservice.models.enums.TransactionType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CallDetails")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallDetails {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private String id;

    String userId;

    String pdUserId;

    BigDecimal leafsTransacted;

    @Enumerated(EnumType.STRING)
    TransactionType callType; // Audio or Video

    private LocalDateTime lastUpdateDate;

    public CallDetails(String userId, String pdUserId, BigDecimal leafsTransacted, TransactionType callType) {
        this.userId = userId;
        this.pdUserId = pdUserId;
        this.leafsTransacted = leafsTransacted;
        this.callType = callType;
        this.lastUpdateDate = LocalDateTime.now();
    }
}
