package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sistema_autorizacion_13")
public class System1320Data {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Builder.Default
    String pc = "BITRIX";
    String usuario;
    double Valor_Documento;
    long nit;
    @Builder.Default
    String Programa = "INTEGRACION BITRIX";
    @Builder.Default
    LocalDateTime fecha_hora = LocalDateTime.now();
    @Builder.Default
    int autorizado = 0;
    String usuario_autorizo;
    String mensaje;
    String chat;
    String tipo_autorizacion;
    int bodega;
    String item;
    int condpagocliente;
    int condpagodocumento;
    @Builder.Default
    int sw = 1;
    String Concepto;
    int Documento;
    String notas;
}
