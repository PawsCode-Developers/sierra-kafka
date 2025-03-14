package com.pawscodes.sierras.kafka.bitrix.model.kafka.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    long id;
    double Valor_Documento;
    String nit;

}
