package com.pawscodes.sierras.kafka.bitrix.model.bitrix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitrixAdd<T> {
    T fields;
}
