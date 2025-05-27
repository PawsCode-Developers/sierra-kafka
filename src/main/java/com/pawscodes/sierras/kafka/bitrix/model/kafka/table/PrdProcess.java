package com.pawscodes.sierras.kafka.bitrix.model.kafka.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrdProcess {
    String sw;
    String bodega;
    String numeroOP;
    String codigo;
    String seq;
    String proceso;
    String actividad;
    String operarioStart;
    String operarioStop;
    String orden;
    String fechaHoraStart;
    String fechaHoraStop;
    String noAplica;
    String notas;
    String fechaHora;
    String usuario;
    String pc;
}
