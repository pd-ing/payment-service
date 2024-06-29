package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {
    Optional<DeviceToken> findByToken(String token);

    Optional<DeviceToken> findByDeviceIdAndToken(String deviceId, String token);

    List<DeviceToken> findByUserId(String userId);

    void deleteByToken(String token);
}
