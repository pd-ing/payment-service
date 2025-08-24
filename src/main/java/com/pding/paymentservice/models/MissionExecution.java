package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ls_mission_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionExecution {
    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String buyerUserId; // Reference to the user who execute the mission

    @Column(nullable = false)
    private String pdUserId;

    @Column(nullable = false)
    private String missionId;

    @Column(nullable = false)
    private BigDecimal treesOffered;

    @Column(nullable = false)
    private LocalDateTime executionDate;

    @Column(nullable = false)
    private String streamId; // Reference to the livestream associated with this mission execution
}