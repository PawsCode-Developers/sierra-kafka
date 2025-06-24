package com.pawscodes.sierras.kafka.bitrix.model.kafka.table;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pawscodes.sierras.kafka.bitrix.config.UnixTimestampToLocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    long id;
    @JsonProperty("Valor_Documento")
    double Valor_Documento;
    String nit;
    String usuario;
    String item;
    int autorizado;
    String usuario_autorizo;
    @JsonDeserialize(using = UnixTimestampToLocalDateTimeDeserializer.class)
    LocalDateTime fecha_hora;
    @JsonDeserialize(using = UnixTimestampToLocalDateTimeDeserializer.class)
    LocalDateTime fecha_hora_a;
    String pc_a;
    String chat;
    String mensaje;
    @JsonProperty("Notas")
    String Notas;
}
