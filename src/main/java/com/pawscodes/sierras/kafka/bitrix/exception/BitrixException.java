package com.pawscodes.sierras.kafka.bitrix.exception;

import lombok.Getter;

public class BitrixException extends Exception {
    @Getter
    private final long bitrixId;
    @Getter
    private final String type;

    public BitrixException(long bitrixId, String type) {
        this.bitrixId = bitrixId;
        this.type = type;
    }

    public BitrixException(String message, long bitrixId, String type) {
        super(message);
        this.bitrixId = bitrixId;
        this.type = type;
    }

    public BitrixException(String message, Throwable cause, long bitrixId, String type) {
        super(message, cause);
        this.bitrixId = bitrixId;
        this.type = type;
    }
}
