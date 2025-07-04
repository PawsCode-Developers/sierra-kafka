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
@Table(name = "terceros")
public class CompanyData {
    @Id
    long nit;
    int id;
    String tipo_identificacion;
    String digito;
    String nombres;
    String bloqueo;
    @OneToOne
    @JoinColumn(name = "id_definicion_tributaria_tipo", insertable = false, updatable = false)
    DefinitionTypeData id_definicion_tributaria_tipo;
    String regimen;
    String fecha_creacion;
    String condicion;
    @OneToOne
    @JoinColumn(name = "y_pais", insertable = false, updatable = false)
    CountryData yPais;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "y_dpto", referencedColumnName = "departamento", insertable = false, updatable = false),
            @JoinColumn(name = "y_pais", referencedColumnName = "pais", insertable = false, updatable = false)
    })
    DepartmentData yDpto;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "y_ciudad", referencedColumnName = "ciudad"),
            @JoinColumn(name = "y_dpto", referencedColumnName = "departamento"),
            @JoinColumn(name = "y_pais", referencedColumnName = "pais")
    })
    CityData yCiudad;
    String direccion;
    String telefono_1;
    String telefono_2;
    String celular;
    String gran_contribuyente;
    String autoretenedor;
    String concepto_4;
    String concepto_5;
    String concepto_6;
    String concepto_7;
    String concepto_8;
    String concepto_9;
    String concepto_14;
    Double cupo_credito;
    String es_excento_iva;
}
