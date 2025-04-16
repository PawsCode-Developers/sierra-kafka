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
@Table(name = "condiciones_pago")
public class PaymentConditionData {
    @Id
    String condicion;
    String descripcion;
    String dias_vcto;
}
