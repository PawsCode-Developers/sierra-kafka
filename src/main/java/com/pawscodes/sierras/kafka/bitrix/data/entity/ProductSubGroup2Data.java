package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.ProductSubGroup2Id;
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
@IdClass(ProductSubGroup2Id.class)
@Table(name = "referencias_sub2")
public class ProductSubGroup2Data {
    @Id
    String grupo;
    @Id
    String subgrupo;
    @Id
    String subgrupo2;
    String descripcion;
}
