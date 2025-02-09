package com.pawcodes.sierra.kafka.bitrix.model.kafka.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    String codigo;
    String descripcion;
    String valor_unitario;
    String porcentaje_iva;
    String grupo;
    String subgrupo;
    String subgrupo2;
    String subgrupo3;
}
