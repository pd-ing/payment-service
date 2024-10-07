package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MessagePurchase")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
public class MessagePurchase {
    @Id
    @UuidGenerator
    private String id;

    String userId;

    String pdUserid;

    BigDecimal leafsTransacted;

    BigDecimal treesTransacted;

    String messageId;

    Boolean isGift = false;

    String giftId;
    private LocalDateTime lastUpdateDate;

    public MessagePurchase(String userId, String pdUserid, BigDecimal leafsTransacted, BigDecimal treesTransacted, String messageId, Boolean isGift, String giftId, LocalDateTime lastUpdateDate) {
        this.userId = userId;
        this.pdUserid = pdUserid;
        this.leafsTransacted = leafsTransacted;
        this.treesTransacted = treesTransacted;
        this.messageId = messageId;
        this.isGift = isGift;
        this.giftId = giftId;
        this.lastUpdateDate = lastUpdateDate;
    }
}
