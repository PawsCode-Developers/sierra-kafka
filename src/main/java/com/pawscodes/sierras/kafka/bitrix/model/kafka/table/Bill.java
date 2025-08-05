package com.pawscodes.sierras.kafka.bitrix.model.kafka.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    String sw;
    String tipo;
    String numero;
    long nit;
    String fecha;
    String documento;
}
