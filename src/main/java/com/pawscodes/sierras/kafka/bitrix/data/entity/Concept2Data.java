package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tipo_transacciones_concep2_ped")
public class Concept2Data {
    int sw;
    int concepto;
    @Id
    String descripcion;
}
