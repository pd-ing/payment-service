package com.pding.paymentservice.controllers;

import com.pding.paymentservice.models.enums.NotificaitonDataType;
import com.pding.paymentservice.payload.request.fcm.DeviceTokenRequest;
import com.pding.paymentservice.payload.request.fcm.SendGenericNotificationRequest;
import com.pding.paymentservice.payload.request.fcm.SendNotificationRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.DeviceTokenService;
import com.pding.paymentservice.service.FcmService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment/tokens")
public class DeviceTokenController {

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    private FcmService fcmService;

    @Autowired
    private AuthHelper authHelper;

    @PostMapping("/register")
    public ResponseEntity<?> registerDeviceToken(@Valid @RequestBody DeviceTokenRequest deviceTokenRequest) {
        try {
            String userId = authHelper.getUserId();
            String msg = deviceTokenService.saveOrUpdateDeviceToken(deviceTokenRequest.getDeviceId(), deviceTokenRequest.getDeviceToken(), userId);
            return ResponseEntity.ok().body(new GenericStringResponse(null, msg + "Device Token Registered/Updated Successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteDeviceToken(@Valid @RequestBody DeviceTokenRequest deviceTokenRequest) {
        try {
            if (deviceTokenRequest.getDeviceId() != null && !deviceTokenRequest.getDeviceId().trim().isEmpty()) {
                if (deviceTokenService.deleteToken(deviceTokenRequest.getDeviceId()))
                    return ResponseEntity.ok().body(new GenericStringResponse(null, "Device Token Deleted Successfully"));
                else
                    return ResponseEntity.badRequest().body(new GenericStringResponse(null, "Not Found : Device Token Not Deleted"));
            } else
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Device ID cannot be null or empty"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }

    }

    // This endpoint will be used only to send gift related notification
    @PostMapping("/sendNotification")
    public ResponseEntity<?> sendNotification(@Valid @RequestBody SendNotificationRequest sendNotificationRequest) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("NotificationType", NotificaitonDataType.GIFT_RECEIVE.getDisplayName());
            data.put("GiftId", sendNotificationRequest.getGiftId());
            data.put("UserId", sendNotificationRequest.getUserId());
            String message = fcmService.sendNotification(sendNotificationRequest.getUserId(), data);
            return ResponseEntity.ok().body(new GenericStringResponse(null, message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // This endpoint will be used to send generic notification
    @PostMapping("/sendGenericNotification")
    public ResponseEntity<?> sendGenericNotification(@RequestBody SendGenericNotificationRequest sendGenericNotificationRequest) {
        try {
            Map<String, String> data = new HashMap<>();
            if (sendGenericNotificationRequest.getData() != null)
                data = sendGenericNotificationRequest.getData();
            String message = fcmService.sendGenericNotification(sendGenericNotificationRequest.getUserId(), data, sendGenericNotificationRequest.getNotificationTitle(), sendGenericNotificationRequest.getNotificationBody());
            return ResponseEntity.ok().body(new GenericStringResponse(null, message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
