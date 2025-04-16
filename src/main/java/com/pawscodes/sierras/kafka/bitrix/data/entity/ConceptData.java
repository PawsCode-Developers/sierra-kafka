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
@Table(name = "tipo_transacciones_concep_ped")
public class ConceptData {
    int sw;
    int concepto;
    @Id
    String descripcion;
}
