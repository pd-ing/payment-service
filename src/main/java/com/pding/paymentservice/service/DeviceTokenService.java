package com.pding.paymentservice.service;

import com.pding.paymentservice.models.DeviceToken;
import com.pding.paymentservice.repository.DeviceTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceTokenService {
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void saveOrUpdateDeviceToken(String deviceId, String deviceToken, String userId) {
        Optional<DeviceToken> optionalDeviceToken = deviceTokenRepository.findByDeviceIdAndToken(deviceId, deviceToken);
        if (optionalDeviceToken.isPresent()) {
            throw new RuntimeException("Token is already registered");
        }

        List<DeviceToken> existingTokens = deviceTokenRepository.findByUserId(userId);

        Optional<DeviceToken> deviceTokenOptional1 = deviceTokenRepository.findOldestDeviceTokenByUserId(userId);
        if (existingTokens.size() >= 5) {
            throw new RuntimeException("Can only add 5 device tokens at max");
        }

        DeviceToken deviceTokenObj = new DeviceToken();
        deviceTokenObj.setDeviceId(deviceId);
        deviceTokenObj.setToken(deviceToken);
        deviceTokenObj.setUserId(userId);
        deviceTokenObj.setCreatedDate(LocalDateTime.now());

        deviceTokenRepository.save(deviceTokenObj);
    }

    public List<DeviceToken> getTokensByUserId(String userId) {
        return deviceTokenRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }
}
