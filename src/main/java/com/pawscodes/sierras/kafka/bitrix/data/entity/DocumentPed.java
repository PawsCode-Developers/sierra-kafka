package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.*;
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
@Table(name = "documentos_ped")
public class DocumentPed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Builder.Default
    int sw = 1;
    int bodega;
    int numero;
    long nit;
    @Builder.Default
    int anulado = 0;
    LocalDate fecha;
    LocalDateTime fecha_hora;
    double valor_total;
    long vendedor;
    String condicion;
    int documento;
    String notas;
    int diasValidez;
    @Builder.Default
    String pc = "BITRIX";
    String usuario;
    String Nit_Usuario;
    @Builder.Default
    int descuento_pie = 0;
    @Builder.Default
    double fletes = 0;
    @Builder.Default
    double iva_fletes = 0;
    @Builder.Default
    int descuento_des_iva = 0;
    @Builder.Default
    int listaPrecios = 1;
    @Builder.Default
    double tasa = 1;
    @Builder.Default
    int concepto = 0;
    @Builder.Default
    int concepto2 = 0;
    @Builder.Default
    int codigo_direccion = 0;
}
