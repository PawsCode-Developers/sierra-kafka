package com.pawscodes.sierras.kafka.bitrix.model.bitrix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitrixGetProduct {
    long id;
    int iblockId;
}
