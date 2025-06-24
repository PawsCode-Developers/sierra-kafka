package com.pawscodes.sierras.kafka.bitrix.data.entity.composeId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrdProcessId implements Serializable {
    String numeroop;
    String codigo;
    String orden;
}
