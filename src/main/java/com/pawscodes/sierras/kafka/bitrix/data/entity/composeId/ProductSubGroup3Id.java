package com.pawscodes.sierras.kafka.bitrix.data.entity.composeId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSubGroup3Id implements Serializable {
    String grupo;
    String subgrupo;
    String subgrupo2;
    String subgrupo3;
}
