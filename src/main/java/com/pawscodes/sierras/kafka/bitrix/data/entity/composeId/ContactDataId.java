package com.pawscodes.sierras.kafka.bitrix.data.entity.composeId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDataId implements Serializable {
    String nit;
    String contacto;
}
