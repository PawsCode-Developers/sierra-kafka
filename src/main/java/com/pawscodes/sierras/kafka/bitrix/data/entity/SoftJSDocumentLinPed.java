package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "softjs_documentos_lin_ped")
public class SoftJSDocumentLinPed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Builder.Default
    int sw = 1;
    int bodega;
    int numero;
    int seq;
    double cantidad;
    double valorunitario;
    int iddoclinped;
    String codigo;
    @Builder.Default
    int cantidad_despachada = 0;
    @Builder.Default
    double porcdcto = 0.00;
    @Builder.Default
    int porcdcto2 = 0;
    @Builder.Default
    int porcdcto3 = 0;
    @Builder.Default
    double fletes = 0;
    String notas;
    int porcentajeiva;
    String und;
    @Builder.Default
    double cantidadund = 1;
    int idsdp;
    double cantidaddos;
    double cantidadotraund;
}
