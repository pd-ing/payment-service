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
    public String saveOrUpdateDeviceToken(String deviceId, String deviceToken, String userId) {
        String deleteMessage = " ";
        Optional<DeviceToken> optionalDeviceToken = deviceTokenRepository.findByDeviceIdAndToken(deviceId, deviceToken);
        if (optionalDeviceToken.isPresent()) {
            throw new RuntimeException("Token is already registered");
        }

        List<DeviceToken> existingTokens = deviceTokenRepository.findByUserId(userId);

        Optional<DeviceToken> deviceTokenOptional1 = deviceTokenRepository.findOldestDeviceTokenByUserId(userId);
        if (existingTokens.size() >= 5) {
           // throw new RuntimeException("Can only add 5 device tokens at max");
            if(deviceTokenOptional1.isPresent()){
                deleteMessage = deleteMessage.trim() + "Device limit reached, deleting oldest device : " + deviceTokenOptional1.get().getDeviceId() + ". ";;
                deviceTokenRepository.delete(deviceTokenOptional1.get());
            }
            else
                throw new RuntimeException("Can only add 5 device tokens at max, Device to Delete Not Found!");
        }

        DeviceToken deviceTokenObj = new DeviceToken();
        deviceTokenObj.setDeviceId(deviceId);
        deviceTokenObj.setToken(deviceToken);
        deviceTokenObj.setUserId(userId);
        deviceTokenObj.setCreatedDate(LocalDateTime.now());

        deviceTokenRepository.save(deviceTokenObj);
        return deleteMessage;
    }

    public List<DeviceToken> getTokensByUserId(String userId) {
        return deviceTokenRepository.findByUserId(userId);
    }

    @Transactional
    public boolean deleteToken(String deviceId) {
        // Check if the device token exists before deletion
        Optional<DeviceToken> tokenBeforeDeletion = deviceTokenRepository.findByDeviceId(deviceId);

        if (tokenBeforeDeletion.isPresent()) {
            // Delete the device token
            deviceTokenRepository.deleteByDeviceId(deviceId);

            // Check if the device token still exists after deletion
            Optional<DeviceToken> tokenAfterDeletion = deviceTokenRepository.findByDeviceId(deviceId);

            // Return true if the token no longer exists, meaning it was successfully deleted
            return tokenAfterDeletion.isEmpty();
        } else {
            // The device token did not exist before deletion attempt
            return false;
        }
    }
}
