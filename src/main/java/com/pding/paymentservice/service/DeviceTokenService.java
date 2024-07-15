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
        String resultMessage = " ";
        Optional<DeviceToken> optionalDeviceToken = deviceTokenRepository.findByDeviceIdAndToken(deviceId, deviceToken);
        if (optionalDeviceToken.isPresent()) {
            throw new RuntimeException("Token is already registered");
        }
        optionalDeviceToken = deviceTokenRepository.findByDeviceId(deviceId);
        if(optionalDeviceToken.isPresent()){
            //update token for existing device
            DeviceToken deviceTokenToUpdate = optionalDeviceToken.get();
            deviceTokenToUpdate.setToken(deviceToken);
            deviceTokenRepository.save(deviceTokenToUpdate);
            resultMessage = resultMessage.trim() + "Device exists, Updating Device : " + deviceTokenToUpdate.getDeviceId() + ". ";
        }
        else {
            // add new device-token combination
            List<DeviceToken> existingTokens = deviceTokenRepository.findByUserId(userId);

            Optional<DeviceToken> deviceTokenOptional1 = deviceTokenRepository.findOldestDeviceTokenByUserId(userId);
            if (existingTokens.size() >= 5) {
                // throw new RuntimeException("Can only add 5 device tokens at max");
                if(deviceTokenOptional1.isPresent()){
                    resultMessage = resultMessage.trim() + "Device limit reached, deleting oldest device : " + deviceTokenOptional1.get().getDeviceId() + ". ";
                    deviceTokenRepository.delete(deviceTokenOptional1.get());
                }
                else
                    throw new RuntimeException("Can only add 5 device tokens at max, Device to Delete Not Found!");
            }

            // Save new
            DeviceToken deviceTokenObj = new DeviceToken();
            deviceTokenObj.setDeviceId(deviceId);
            deviceTokenObj.setToken(deviceToken);
            deviceTokenObj.setUserId(userId);
            deviceTokenObj.setCreatedDate(LocalDateTime.now());

            deviceTokenRepository.save(deviceTokenObj);
        }

        return resultMessage;
    }

    public List<DeviceToken> getTokensByUserId(String userId) {
        return deviceTokenRepository.findByUserId(userId);
    }

    @Transactional
    public boolean deleteToken(String token, String userId) {
        Optional<DeviceToken> tokenToBeDeleted = deviceTokenRepository.findByTokenAndUserId(token, userId);
        if(tokenToBeDeleted.isPresent())
        {
            deviceTokenRepository.deleteByTokenAndUserId(token, userId);
            Optional<DeviceToken> deletedToken = deviceTokenRepository.findByTokenAndUserId(token, userId);
            return deletedToken.isEmpty();
        }
        else
            return false;

    }
}
