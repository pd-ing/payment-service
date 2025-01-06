package com.pding.paymentservice.models;

import com.pding.paymentservice.models.enums.ExposureTicketStatus;
import com.pding.paymentservice.models.enums.ExposureTicketType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;

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

    private Instant purchasedDate;

    private Instant usedDate;

    private ExposureTicketStatus status;

}
