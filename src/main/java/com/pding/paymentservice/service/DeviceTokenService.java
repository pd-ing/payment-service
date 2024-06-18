package com.pding.paymentservice.service;

import com.pding.paymentservice.models.DeviceToken;
import com.pding.paymentservice.repository.DeviceTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceTokenService {
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void saveOrUpdateDeviceToken(String token, String userId) {
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByToken(token);
        if (existingToken.isPresent()) {
            DeviceToken deviceToken = existingToken.get();
            deviceToken.setUserId(userId);
            deviceTokenRepository.save(deviceToken);
        } else {
            DeviceToken deviceToken = new DeviceToken();
            deviceToken.setToken(token);
            deviceToken.setUserId(userId);
            deviceTokenRepository.save(deviceToken);
        }
    }

    public List<DeviceToken> getTokensByUserId(String userId) {
        return deviceTokenRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }
}
