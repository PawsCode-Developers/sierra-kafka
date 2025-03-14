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
@Table(name = "referencias")
public class ProductData {
    @Id
    String codigo;
    String descripcion;
    double valor_unitario;
    double costo_unitario;
    double porcentaje_iva;
    String grupo;
    String subgrupo;
    String subgrupo2;
    String subgrupo3;
}
