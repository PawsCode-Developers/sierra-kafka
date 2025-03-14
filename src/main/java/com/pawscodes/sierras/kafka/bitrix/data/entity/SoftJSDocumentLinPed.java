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
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;
    @Builder.Default
    int sw = 1;
    int bodega;
    int numero;
    int seq;
    double cantidad;
    double valorUnitario;
    int idDocLinPed;
}
