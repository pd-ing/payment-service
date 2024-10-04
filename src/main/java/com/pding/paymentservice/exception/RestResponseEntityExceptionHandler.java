package com.pding.paymentservice.exception;

import com.pding.paymentservice.payload.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ AuthenticationException.class })
    @ResponseBody
    public ResponseEntity handleAuthenticationException(Exception authException, HttpServletRequest request) {

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "UNAUTHORIZED REQUEST");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());
        body.put("jwtErrors", request.getAttribute("jwtErrors"));
        body.put("authErrorCode", request.getAttribute("authErrorCode"));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }


    @ExceptionHandler({ Exception.class })
    @ResponseBody
    public ResponseEntity handleEx(Exception exception, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage()));
    }

}
