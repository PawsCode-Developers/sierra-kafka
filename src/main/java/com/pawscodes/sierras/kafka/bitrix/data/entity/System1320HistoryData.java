package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sistema_autorizacion_13_his")
public class System1320HistoryData {
    @Id
    long id;
    @Builder.Default
    String pc = "BITRIX";
    String usuario;
    double Valor_Documento;
    long nit;
    @Builder.Default
    String programa = "INTEGRACION BITRIX";
    @Builder.Default
    LocalDateTime fecha_hora = LocalDateTime.now(ZoneId.of("America/Bogota"));
    int autorizado;
    String usuario_autorizo;
    LocalDateTime fecha_hora_a;
    String pc_a;
    String mensaje;
    String chat;
}
