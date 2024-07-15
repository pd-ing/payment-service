package com.pding.paymentservice.controllers;

import com.pding.paymentservice.models.enums.NotificaitonDataType;
import com.pding.paymentservice.payload.request.fcm.DeviceTokenRequest;
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
            deviceTokenService.saveOrUpdateDeviceToken(deviceTokenRequest.getToken(), userId);
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Device Token Registered/Updated Successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteDeviceToken(@Valid @RequestBody DeviceTokenRequest deviceTokenRequest) {
        try {
            String userId = authHelper.getUserId();
            boolean isDeleted = deviceTokenService.deleteToken(deviceTokenRequest.getToken(), userId);
            if(isDeleted)
                return ResponseEntity.ok().body(new GenericStringResponse(null, "Device Token Deleted Successfully"));
            else
                return ResponseEntity.badRequest().body(  new ErrorResponse(HttpStatus.BAD_REQUEST.value(),"Failed to delete"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }

    }

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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
