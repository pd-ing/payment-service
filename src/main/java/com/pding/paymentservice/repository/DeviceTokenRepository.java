package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {
    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByUserId(String userId);

    Optional<DeviceToken> findByTokenAndUserId(String token, String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken d WHERE d.token = :token AND d.userId = :userId")
    void deleteByTokenAndUserId(@Param("token") String token, @Param("userId") String userId);
}
