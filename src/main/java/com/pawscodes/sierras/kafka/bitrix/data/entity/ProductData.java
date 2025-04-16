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
@Table(name = "referencias")
public class ProductData {
    @Id
    String codigo;
    String descripcion;
    double valor_unitario;
    double costo_unitario;
    double porcentaje_iva;
    @OneToOne
    @JoinColumn(name = "grupo", insertable = false, updatable = false)
    ProductGroupData grupo;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "grupo", referencedColumnName = "grupo", insertable = false, updatable = false),
            @JoinColumn(name = "subgrupo", referencedColumnName = "subgrupo", insertable = false, updatable = false)
    })
    ProductSubGroupData subgrupo;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "grupo", referencedColumnName = "grupo", insertable = false, updatable = false),
            @JoinColumn(name = "subgrupo", referencedColumnName = "subgrupo", insertable = false, updatable = false),
            @JoinColumn(name = "subgrupo2", referencedColumnName = "subgrupo2", insertable = false, updatable = false)
    })
    ProductSubGroup2Data subgrupo2;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "grupo", referencedColumnName = "grupo", insertable = false, updatable = false),
            @JoinColumn(name = "subgrupo", referencedColumnName = "subgrupo", insertable = false, updatable = false),
            @JoinColumn(name = "subgrupo2", referencedColumnName = "subgrupo2", insertable = false, updatable = false),
            @JoinColumn(name = "subgrupo3", referencedColumnName = "subgrupo3", insertable = false, updatable = false)
    })
    ProductSubGroup3Data subgrupo3;
    String maneja_otra_und;
    String otra_und;
}
