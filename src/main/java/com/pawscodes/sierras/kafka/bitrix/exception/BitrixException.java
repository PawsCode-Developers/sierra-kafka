package com.pawscodes.sierras.kafka.bitrix.exception;

public class BitrixException extends Exception {
    public BitrixException() {
    }

    public BitrixException(String message) {
        super(message);
    }

    public BitrixException(String message, Throwable cause) {
        super(message, cause);
    }
}
