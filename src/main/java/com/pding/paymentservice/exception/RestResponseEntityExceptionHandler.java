package com.pding.paymentservice.exception;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    @ExceptionHandler({ NoSuchElementException.class })
    @ResponseBody
    public ResponseEntity handleNotFoundException(NoSuchElementException exception, HttpServletRequest request) {
        log.error(exception.getMessage(), exception);

        String message = exception.getMessage();
        String errorCode = null;

        // Extract error code if message is in format "CODE: message"
        if (message != null && message.contains(":")) {
            String[] parts = message.split(":", 2);
            errorCode = parts[0];
            message = parts[1].trim();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new GenericClassResponse(new ErrorResponse(HttpStatus.NOT_FOUND.value(), message, errorCode, null), null));
    }

    @ExceptionHandler({ Exception.class })
    @ResponseBody
    public ResponseEntity handleEx(Exception exception, HttpServletRequest request) {
        log.error(exception.getMessage(), exception);

        String message = exception.getMessage();
        String errorCode = null;

        // Extract error code if message is in format "CODE: message"
        if (message != null && message.contains(":")) {
            String[] parts = message.split(":", 2);
            errorCode = parts[0];
            message = parts[1].trim();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new GenericClassResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, errorCode, null), null));
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    @ResponseBody
    public ResponseEntity handleBadRequestEx(Exception exception, HttpServletRequest request) {
        String message = exception.getMessage();
        String errorCode = null;

        // Extract error code if message is in format "CODE: message"
        if (message != null && message.contains(":")) {
            String[] parts = message.split(":", 2);
            errorCode = parts[0];
            message = parts[1].trim();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new GenericClassResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, errorCode, null), message));
    }

}
