package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documentos_ped_historia")
public class DocumentPedHistory {
    @Builder.Default
    int sw = 1;
    int bodega;
    @Id
    int numero;
    long nit;
    long vendedor;
    LocalDate fecha;
    String condicion;
    int diasValidez;
    double valor_total;
    LocalDateTime fecha_hora;
    @Builder.Default
    int anulado = 0;
    String notas;
    String usuario;
    @Builder.Default
    String pc = "BITRIX";
    @Builder.Default
    int codigo_direccion = 0;
    String documento;
    @Builder.Default
    double fletes = 0;
    @Builder.Default
    double iva_fletes = 0;
    @Builder.Default
    int listaPrecios = 1;
    @Builder.Default
    int concepto2 = 0;
}
