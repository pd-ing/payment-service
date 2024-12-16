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
@Table(name = "CallPurchase")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallPurchase {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private String id;

    String userId;

    String pdUserId;

    BigDecimal leafsTransacted;
    BigDecimal treesTransacted;

    @Enumerated(EnumType.STRING)
    TransactionType callType; // Audio or Video

    String callId;

    String giftId;

    private LocalDateTime lastUpdateDate;

    public CallPurchase(String userId, String pdUserId, BigDecimal leafsTransacted, BigDecimal treesTransacted, TransactionType callType, String callId, String giftId) {
        this.userId = userId;
        this.pdUserId = pdUserId;
        this.leafsTransacted = leafsTransacted;
        this.treesTransacted = treesTransacted;
        this.callType = callType;
        this.lastUpdateDate = LocalDateTime.now();
        this.callId = callId;
        this.giftId = giftId;
    }
}
