package com.pawscodes.sierras.kafka.bitrix.model.kafka.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    String tipo_identificacion;
    String nit;
    String digito;
    String nombres;
    String bloqueo;
    String id_definicion_tributaria_tipo;
    String regimen;
    String fecha_creacion;
    String concepto_14;
    String condicion;
    String y_pais;
    String y_dpto;
    String y_ciudad;
    String direccion;
    String telefono_1;
    String telefono_2;
    String celular;
    String gran_contribuyente;
    String autoretenedor;
    String concepto_4;
    String concepto_5;
    String concepto_6;
    String concepto_7;
    String concepto_8;
    String concepto_9;
}
