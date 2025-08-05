package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.CompanyDirId;
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
@IdClass(CompanyDirId.class)
@Table(name = "terceros_dir")
public class CompanyDirData {
    @Id
    long nit;
    @Id
    int codigoDireccion;
    String direccion;
    String ciudad;
    String dir_activa;
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
}
