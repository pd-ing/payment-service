package com.pding.paymentservice.exception;

public class InvalidTransactionIDException extends RuntimeException {
    public InvalidTransactionIDException(String message) {
        super(message);
    }
}
