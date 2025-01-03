package com.pding.paymentservice.models;

import com.pding.paymentservice.models.enums.ExposureTicketType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
public class MExposureTicket {
    @Id
    @Enumerated(EnumType.STRING)
    private ExposureTicketType type;
    private BigDecimal price;
}
