package com.pawscodes.sierras.kafka.bitrix.model.kafka.table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pawscodes.sierras.kafka.bitrix.config.UnixTimestampToLocalDateDeserializer;
import com.pawscodes.sierras.kafka.bitrix.config.UnixTimestampToLocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrdPlanProcess {
    String sw;
    String bodega;
    String numero;
    @JsonDeserialize(using = UnixTimestampToLocalDateTimeDeserializer.class)
    LocalDateTime fechaOrden;
    @JsonDeserialize(using = UnixTimestampToLocalDateDeserializer.class)
    LocalDate fechaEntregaCliente;
    String porcentajePlaneado;
}
