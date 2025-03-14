package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.ProductSubGroup3Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(ProductSubGroup3Id.class)
@Table(name = "referencias_sub")
public class ProductSubGroup3Data {
    @Id
    String grupo;
    @Id
    String subgrupo;
    @Id
    String subgrupo2;
    @Id
    String subgrupo3;
    String descripcion;
}
