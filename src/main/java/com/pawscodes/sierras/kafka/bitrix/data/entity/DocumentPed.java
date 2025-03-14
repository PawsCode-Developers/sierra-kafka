package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.DocumentPedId;
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
@IdClass(DocumentPedId.class)
@Table(name = "documentos_ped")
public class DocumentPed {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;
    @Id
    @Builder.Default
    int sw = 1;
    @Id
    int bodega;
    @Id
    int numero;
    long nit;
    @Builder.Default
    int anulado = 0;
    LocalDateTime fecha;
    double valor_total;
}
