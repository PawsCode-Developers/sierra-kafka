package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Table(name = "documentos_lin_ped")
public class DocumentLinPed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Builder.Default
    int sw = 1;
    int bodega;
    int numero;
    int seq;
    double cantidad;
    @JsonProperty("valor_unitario")
    double valorUnitario;
    String codigo;
    @Builder.Default
    double cantidad_despachada = 0;
    int porcentaje_iva;
    @Builder.Default
    int porcentaje_descuento = 0;
    String und;
    @Builder.Default
    double cantidad_und = 1;
    @Builder.Default
    char adicional = ' ';
    @Builder.Default
    int porc_dcto_2 = 0;
    @Builder.Default
    int porc_dcto_3 = 0;
    double despacho_virtual;
    double cantidad_otra_und;
    double cantidad_dos;
}
