package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.CompanyDirId;
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
    String y_dpto;
    String y_ciudad;
    String y_pais;
}
