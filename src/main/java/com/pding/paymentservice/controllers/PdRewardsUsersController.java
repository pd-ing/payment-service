package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.request.PdRewardUsersRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.service.PdRewardsUsersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class PdRewardsUsersController {

    @Autowired
    PdLogger pdLogger;

    @Autowired
    PdRewardsUsersService pdRewardsUsersService;

    @PostMapping(value = "/updateRewardSettings")
    public ResponseEntity<?> updateRewardSettings(@Valid @RequestBody PdRewardUsersRequest pdRewardUsersRequest) {

        try {
            String rewardsForUsers = pdRewardUsersRequest.toJsonString();
            String stringResponse = pdRewardsUsersService.updateRewardSettings(pdRewardUsersRequest.getDescription(), rewardsForUsers);

            return ResponseEntity.ok().body(new GenericStringResponse(null, stringResponse));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.UPDATE_REWARD_SETTING, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/getRewardSettings")
    public ResponseEntity<?> getRewardSettings() {
        try {
            PdRewardUsersRequest pdRewardUsersRequest = pdRewardsUsersService.getRewardSettings();
            return ResponseEntity.ok().body(new GenericClassResponse<>(null, pdRewardUsersRequest));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.GET_REWARD_SETTING, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericClassResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
