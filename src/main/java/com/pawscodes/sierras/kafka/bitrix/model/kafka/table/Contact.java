package com.pawscodes.sierras.kafka.bitrix.model.kafka.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    String nit;
    String contacto;
    String nombre;
    String apellidos;
    String tel_ofi1;
    String tel_ofi2;
    String tel_celular;
    String e_mail;
    String cargo;
    String ext1;
}
