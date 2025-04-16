package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.CityId;
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
@IdClass(CityId.class)
@Table(name = "y_ciudades")
public class CityData {
    @Id
    String ciudad;
    String descripcion;
    @Id
    String departamento;
    @Id
    String pais;
}
