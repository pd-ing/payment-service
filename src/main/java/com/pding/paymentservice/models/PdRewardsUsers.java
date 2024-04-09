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
@Table(name = "PdRewardsUsers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdRewardsUsers {
    @Id
    @UuidGenerator
    private String id;

    private String pdUserId;

    @Column(name = "description", columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String description;

    //This will store the json
    @Column(name = "rewardForTopUsers", columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String rewardForTopUsers;

    private LocalDateTime lastUpdateDate;
}
