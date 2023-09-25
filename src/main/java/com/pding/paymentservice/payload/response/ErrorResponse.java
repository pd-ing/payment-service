package com.pding.paymentservice.payload.response;

import java.util.Date;

public class ErrorResponse {
    private int status;
    private String message;
    private Date timestamp;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = new Date();
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}