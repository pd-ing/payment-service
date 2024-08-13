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

@Entity
@Table(name = "InChatMediaTrading")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InChatMediaTrading {
    @Id
    @UuidGenerator
    private String id;
    private String messageId;
    private String userId;
    private String pdId;
    private String leafsToCharge;
    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String attachments;
    private Boolean isCancel;
    private String transactionStatus;
}
