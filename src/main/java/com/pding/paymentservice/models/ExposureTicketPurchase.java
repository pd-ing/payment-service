package com.pding.paymentservice.models;

import com.pding.paymentservice.models.enums.ExposureTicketStatus;
import com.pding.paymentservice.models.enums.ExposureTicketType;
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
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
public class ExposureTicketPurchase {
    @Id
    @UuidGenerator
    private String id;

    private ExposureTicketType type;

    private String userId;

    private BigDecimal treesConsumed;

    private LocalDateTime purchasedDate;

    private LocalDateTime usedDate;

    private ExposureTicketStatus status;

}
