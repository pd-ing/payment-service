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

import java.time.LocalDateTime;

@Entity
@Table(name = "DeviceToken")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceToken {
    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String token;

    @Column(unique = true, nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String userId;

    private LocalDateTime createdDate;
}
