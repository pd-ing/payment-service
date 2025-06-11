package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private Date timestamp;
    private String errorCode;
    private String resolutionCode;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.errorCode = null;
        this.timestamp = new Date();
    }

    public ErrorResponse(int status, String message, String errorCode, String resolutionCode) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.resolutionCode = resolutionCode;
        this.timestamp = new Date();
    }
}
