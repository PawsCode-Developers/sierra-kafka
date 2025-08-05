package com.pawscodes.sierras.kafka.bitrix.exception;

import com.pawscodes.sierras.kafka.bitrix.model.bitrix.BitrixDeal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SecondUnitException extends Exception {
    private BitrixDeal deal;

    public SecondUnitException(BitrixDeal deal, String message) {
        super(message);
        this.deal = deal;
    }
}
