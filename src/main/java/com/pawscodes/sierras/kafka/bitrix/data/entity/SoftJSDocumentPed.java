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
@Table(name = "softjs_documentos_ped")
public class SoftJSDocumentPed {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;
    @Builder.Default
    int sw = 1;
    int bodega;
    int numero;
    long nit;
    @Builder.Default
    int anulado = 0;
    LocalDateTime fecha;
    double valor_total;
    int idDocPed;
}
