package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
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
public class MExposureSlotHistory {
    @Id
    @UuidGenerator
    private String id;

    private String userId;
    private Instant startTime;
    private Instant endTime;
    private String slotNumber;
    private Instant releasedTime;
    private Boolean isForcedRelease;
    private String ticketType;
}
