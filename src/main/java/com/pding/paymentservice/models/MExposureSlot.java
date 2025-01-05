package com.pding.paymentservice.models;

import com.pding.paymentservice.models.enums.ExposureSlotNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
public class MExposureSlot {
    @Id
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_number", unique = true)
    private ExposureSlotNumber slotNumber;
}
