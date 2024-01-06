package com.pding.paymentservice.exception;

import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

public class InsufficientLeafsException extends RuntimeException {
    public InsufficientLeafsException(String message) {
        super(message);
    }
}
