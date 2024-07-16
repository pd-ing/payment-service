package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.DeviceToken;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.PageRequest.*;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {
    Optional<DeviceToken> findByToken(String token);

    Optional<DeviceToken> findByDeviceId(String deviceId);

    Optional<DeviceToken> findByDeviceIdAndToken(String deviceId, String token);

    List<DeviceToken> findByUserId(String userId);

    Optional<DeviceToken> findByTokenAndUserId(String token, String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken d WHERE d.token = :token AND d.userId = :userId")
    void deleteByTokenAndUserId(@Param("token") String token, @Param("userId") String userId);
    void deleteByDeviceId(String deviceId);

    @Query("SELECT dt FROM DeviceToken dt WHERE dt.userId = :userId ORDER BY dt.createdDate ASC")
    List<DeviceToken> findDeviceTokensByUserId(@Param("userId") String userId, Pageable pageable);

    default Optional<DeviceToken> findOldestDeviceTokenByUserId(String userId) {
        Pageable pageable = of(0, 1, Sort.by(Sort.Direction.ASC, "createdDate"));
        List<DeviceToken> tokens = findDeviceTokensByUserId(userId, pageable);
        return tokens.isEmpty() ? Optional.empty() : Optional.of(tokens.get(0));
    }
}
