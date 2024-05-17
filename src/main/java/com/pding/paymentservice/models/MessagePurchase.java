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

    String messageId;

    public MessagePurchase(String userId, String pdUserid, BigDecimal leafsTransacted, String messageId) {
        this.userId = userId;
        this.pdUserid = pdUserid;
        this.leafsTransacted = leafsTransacted;
        this.messageId = messageId;
    }
}
